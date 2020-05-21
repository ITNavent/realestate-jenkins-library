def call(String slackChannel = '#deploys-realestate') {
	container('helm') {
		withCredentials([string(credentialsId: 'deploy_helper_token', variable: 'SLACK_TOKEN')]) {
			script {
				def fields = [:]
				fields['change_success'] = "1"
				echo "fields " + fields.toString()
				def tags = [:]
				tags['job_name']      = env.JOB_NAME
				tags['build_number']  = env.BUILD_NUMBER
				tags['git_commit']    = env.GIT_COMMIT ?: ""
				tags['git_branch']    = env.GIT_BRANCH ?: ""
				echo "tags " + tags.toString();
				influxDbPublisher(customData: fields, customDataTags: tags, selectedTarget: 'influxdb-redeoall', measurementName: 'deploy')
				cfrDeployHelper(env.JOB_NAME, env.GIT_BRANCH, slackChannel, "${SLACK_TOKEN}")
			}
		}
	}
}