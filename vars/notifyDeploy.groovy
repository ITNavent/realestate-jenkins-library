def call(Map config) {
	def slackColor = config?.slackColor ?: 'good'
	def slackChannel = config?.slackChannel ?: '#deploys-realestate'
	def gitRevision = config?.gitRevision ?: 'sin datos'
	def gitBranch = config?.gitBranch ?: 'sin datos'
	def newrelicAppName = config?.newrelicAppName ?: ''
	withCredentials([string(credentialsId: 'newrelic_api_key', variable: 'NR_API_KEY')]) {
		wrap([$class: 'BuildUser']) {
			def buildUserId = "${BUILD_USER_ID}".equals("")? "remoteJob": "${BUILD_USER_ID}" 
			def slackMessage = config?.slackMessage ?: "Deploy finalizado de *${newrelicAppName}* el ${BUILD_TIMESTAMP} \nusuario: ${buildUserId} revision: ${gitRevision} branch: ${gitBranch}"
			slackSend(color: slackColor, channel: slackChannel, message: slackMessage)
			if(newrelicAppName != null && !''.equals(newrelicAppName)) {
				def nameResponse = sh(script: "curl -X GET 'https://api.newrelic.com/v2/applications.json' -H 'X-Api-Key:${NR_API_KEY}' -d 'filter[name]=${newrelicAppName}'", returnStdout: true)
				def jsonResponse = readJSON text: nameResponse
				def newrelicAppId = jsonResponse.applications[0].id
				sh """
				curl -X POST -H 'Content-Type: application/json' \
				-H 'X-Api-Key:${NR_API_KEY}' \
				-d \'{"deployment": { "revision": "${gitRevision}", "user": "${buildUserId}" } }\' https://api.newrelic.com/v2/applications/${newrelicAppId}/deployments.json
        		"""
			}
		}
	}
}