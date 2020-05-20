def call(String jobName, String appVersion, String slackChannel, String slackToken) {
	def safeSlackChannel = slackChannel.startsWith('#')?slackChannel.substring(1):slackChannel
	sh(script: "curl 'https://slack.com/api/chat.postMessage?token=${slackToken}&channel=%23${safeSlackChannel}&text=%2Fdeployhelper%20cfr%20${appName}%20${appVersion}&pretty=1'", returnStdout: false)
}
