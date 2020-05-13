def call(String sha1Commit, String newRelicProjectNameToShow, String baseCommit = 'origin/master', String channelId = '#deploys-realestate') {
	script {
		def IS_ANCESTOR = sh(script: "git merge-base --is-ancestor ${baseCommit} ${sha1Commit} && echo yes || echo no", returnStdout: true).trim()
		if(IS_ANCESTOR != "yes") {
			def msg = "El commit ${baseCommit} no es ancestro de ${sha1Commit}, deteniendo jenkins job en ${newRelicProjectNameToShow}."
			slackSend(color: 'danger', channel: channelId, message: msg, title: "${newRelicProjectNameToShow}")
			currentBuild.result = 'ABORTED'
			error(msg)
		}
	}
}