def call(String statefulsetName, String releaseName, String vsName, String namespace, String gitProject, String chartLocation, String credentialsId) {
	def DOWN_COLOR = ""
	script {
		def UP_COLOR = getBlueGreenDeploymentColour(statefulsetName, vsName, namespace)
		if(UP_COLOR == "blue") {
			DOWN_COLOR = "green"
		} else if(UP_COLOR == "green") {
			DOWN_COLOR = "blue"
		} else {
			currentBuild.result = 'ABORTED'
			error("'${UP_COLOR}' no es un color de deploy valido, debe ser uno de ['blue', 'green']")
		}
		//Copio la cantidad de replicas actuales
		def DOWN_JSON = sh(returnStdout: true, script: "kubectl get statefulset ${statefulsetName}-${DOWN_COLOR} -n ${namespace} -o json")
		def DOWN_PROPS = readJSON text: DOWN_JSON
		def DOWN_REPLICAS = DOWN_PROPS.status.replicas
		def DOWN_TAG_NAME = DOWN_PROPS.metadata.labels['app.kubernetes.io/version']
		echo "DOWN_TAG_NAME ${DOWN_TAG_NAME}"
		checkout([
			$class: 'GitSCM',
			branches: [[name: "refs/tags/${DOWN_TAG_NAME}"]],
			extensions: [
				[$class: 'CleanBeforeCheckout']
			],
			userRemoteConfigs: [[
				credentialsId: credentialsId, 
				url: "git@github.com:ITNavent/${gitProject}.git"
			]]
		])
		sh """
		kubectl scale --replicas=${DOWN_REPLICAS} statefulset/${statefulsetName}-${UP_COLOR} -n ${namespace}
		kubectl wait-sts ${statefulsetName}-${UP_COLOR} -n ${namespace} --timeout 5m
		"""
		updateVirtualServiceReuseValues(statefulsetName, releaseName, namespace, UP_COLOR, chartLocation)
	}
}