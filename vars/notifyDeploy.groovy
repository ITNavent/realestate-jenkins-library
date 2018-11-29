def call(String revision = 'REVISION', String slackChannel = '#deploys-realestate', String newrelicAppName = '') {
	withCredentials([file(credentialsId: 'newrelic_api_key', variable: 'NR_API_KEY')]) {
		wrap([$class: 'BuildUser']) {
			slackSend(color: 'good', channel: slackChannel, message: 'Deploy de ${newrelicAppName} finalizado en ${$BUILD_TIMESTAMP} revision: ${env.GIT_COMMIT} branch: ${env.GIT_BRANCH})
			if(newrelicAppName != null && !''.equals(newrelicAppName)) {
				def nameResponse = sh(script: 'curl -X GET "https://api.newrelic.com/v2/applications.json" -H "X-Api-Key:${NR_API_KEY}" -i -d "filter[name]=${newrelicAppName}"', returnStdout: true)
				def jsonResponse = readJSON text: "${nameResponse}"
				def newrelicAppName = jsonResponse.applications[0].id
				sh """
				curl -X POST -H 'Content-Type: application/json' \
				-H 'X-Api-Key:${NR_API_KEY}' -i \
				-d \'{"deployment": { "revision": "${revision}", "user": "${BUILD_USER_ID}" } }\' https://api.newrelic.com/v2/applications/${newrelicAppName}/deployments.json
        		"""
			}
		}
	}
}