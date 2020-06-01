def call(String jobName, String appVersion, String slackChannel, String slackToken) {
	def safeSlackChannel = slackChannel.startsWith('#')?slackChannel.substring(1):slackChannel
	sh(script: "curl 'https://slack.com/api/chat.postMessage?token=${slackToken}&channel=%23${safeSlackChannel}&text=%40deployhelper%20status%20${jobName}%20${appVersion}&link_names=1&pretty=1'", returnStdout: false)
}
