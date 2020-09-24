def call(Map config) {
	def slackColor = config?.slackColor ?: 'good'
	def slackChannel = config?.slackChannel ?: '#deploys-realestate'
	def gitRevision = config?.gitRevision ?: 'sin datos'
	def gitBranch = config?.gitBranch ?: 'sin datos'
	def newrelicAppName = config?.newrelicAppName ?: ''
	def newrelicDeploy = config?.newrelicDeploy ?: true
	def kubeCurrentContext = sh(script: "kubectl config current-context", returnStdout: true).trim()
	if(!kubeCurrentContext.endsWith("prd") && !kubeCurrentContext.endsWith("pro")) {
		newrelicDeploy = false
	}
	withCredentials([string(credentialsId: 'newrelic_api_key', variable: 'NR_API_KEY')]) {
		def safeBuildUserId = "unknown"
		wrap([$class: 'BuildUser']) {
			try {
				safeBuildUserId = BUILD_USER_ID
			 } catch (e) {
				echo "Build user id not in scope, probably triggered from another job"
			 } 
			def slackMessage = config?.slackMessage ?: "Deploy finalizado de *${newrelicAppName}* el ${BUILD_TIMESTAMP} \nusuario: ${safeBuildUserId} revision: ${gitRevision} branch: ${gitBranch} ${BUILD_URL}"
			slackSend(color: slackColor, channel: slackChannel, message: slackMessage)
			if(newrelicDeploy) {
				def nameResponse = sh(script: "curl -X GET 'https://api.newrelic.com/v2/applications.json' -H 'X-Api-Key:${NR_API_KEY}' -d 'filter[name]=${newrelicAppName}'", returnStdout: true)
				def jsonResponse = readJSON text: nameResponse
				if(jsonResponse.applications.size() > 0) {
					def newrelicAppId = jsonResponse.applications[0].id
					sh """
					curl -X POST -H 'Content-Type: application/json' \
					-H 'X-Api-Key:${NR_API_KEY}' \
					-d \'{"deployment": { "revision": "${gitRevision}", "user": "${safeBuildUserId}" } }\' https://api.newrelic.com/v2/applications/${newrelicAppId}/deployments.json
					"""
				}
			}
		}
	}
}