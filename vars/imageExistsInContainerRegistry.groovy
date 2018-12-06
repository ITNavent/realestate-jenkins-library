def call(String imageName, String tagName, String projectName = 'redeo-all') {
	withCredentials([file(credentialsId: 'jenkins-${projectName}', variable: 'CHECK_PULL_KEYFILE')]) {
		sh """
		gcloud auth activate-service-account --key-file=${CHECK_PULL_KEYFILE} --project ${projectName}
		"""
		def count = sh(script: "gcloud container images list-tags --filter='tags=(${tagName})' --format='table[no-heading](digest)' 'gcr.io/${projectName}/${imageName}' | wc -l", returnStdout: true)
		return !("0" == count.trim())
	}
} 