def call(String imageName, String tagName, String projectName = 'redeo-all') {
	def count = sh(script: "gcloud container images list-tags --filter='tags=(${tagName})' --format='table[no-heading](digest)' 'gcr.io/${projectName}/${imageName}' | wc -l", returnStdout: true)
	echo count
	return !("0" == count) 
} 