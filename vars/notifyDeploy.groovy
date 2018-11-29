def call(Map config) {
	def slackColor = config?.slackColor ?: 'good'
	def slackChannel = config?.slackChannel ?: '#deploys-realestate'
	def gitRevision = config?.gitRevision ?: 'sin datos'
	def gitBranch = config?.gitBranch ?: 'sin datos'
	def newrelicAppName = config?.newrelicAppName ?: ''
	withCredentials([string(credentialsId: 'newrelic_api_key', variable: 'NR_API_KEY')]) {
		wrap([$class: 'BuildUser']) {
			def slackMessage = config?.slackMessage ?: "Deploy por ${BUILD_USER_ID} para ${newrelicAppName} finalizado el ${BUILD_TIMESTAMP} revision: ${gitRevision} branch: ${gitBranch}"
			slackSend(color: slackColor, channel: slackChannel, message: slackMessage)
			if(newrelicAppName != null && !''.equals(newrelicAppName)) {
				def nameResponse = sh(script: "curl -X GET 'https://api.newrelic.com/v2/applications.json' -H 'X-Api-Key:${NR_API_KEY}' -d 'filter[name]=${newrelicAppName}'", returnStdout: true)
				//echo nameResponse
				//def slurper = new groovy.json.JsonSlurper()
				//def jsonResponse = slurper.parseText(nameResponse)
				def jsonResponse = readJSON text: nameResponse
				def newrelicAppId = jsonResponse.applications[0].id
				sh """
				curl -X POST -H 'Content-Type: application/json' \
				-H 'X-Api-Key:${NR_API_KEY}' -i \
				-d \'{"deployment": { "revision": "${gitRevision}", "user": "${BUILD_USER_ID}" } }\' https://api.newrelic.com/v2/applications/${newrelicAppId}/deployments.json
        		"""
			}
		}
	}
}