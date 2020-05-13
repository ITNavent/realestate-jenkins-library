def call(String sha1Commit, String newRelicProjectNameToShow, String baseCommit = 'origin/master', String channelId = '#deploys-realestate') {
	script {
		def IS_ANCESTOR = sh(script: "git merge-base --is-ancestor ${baseCommit} ${sha1Commit} && echo yes || echo no", returnStdout: true)
		echo "IS_ANCESTOR " + IS_ANCESTOR
		if(IS_ANCESTOR != "yes") {
			def msg = "El commit ${sha1Commit} no es ancestro de ${baseCommit}, deteniendo jenkins job en ${newRelicProjectNameToShow}."
			slackSend(color: 'danger', channel: channelId, message: msg, title: "${newRelicProjectNameToShow}")
			currentBuild.result = 'ABORTED'
			error(msg)
		}
	}
}