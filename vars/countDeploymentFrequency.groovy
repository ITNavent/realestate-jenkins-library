def call() {
	script {
		def fields = [:]
		fields['change_sucess'] = 1
		def tags = [:]
		tags['job_name']      = env.JOB_NAME
		tags['build_number']  = env.BUILD_NUMBER
		tags['git_commit']    = env.GIT_COMMIT
		tags['git_branch']    = env.GIT_BRANCH
		influxDbPublisher(customData: fields, customDataTags: tags, selectedTarget: 'influxdb-redeoall', measurementName: 'deploy')
	}
}