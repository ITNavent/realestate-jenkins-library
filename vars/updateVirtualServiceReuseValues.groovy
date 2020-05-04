def call(String statefulsetName, String releaseName, String namespace, String colour) {
	def DOWN_COLOR = ""
	def VS_MAP = ["blue": 0, "green": 0]
	script {
		if(colour == "blue") {
			VS_MAP[colour] = 100
			DOWN_COLOR = "green"
		}
		else if(colour == "green") {
			VS_MAP[colour] = 100
			DOWN_COLOR = "blue"
		} else {
			currentBuild.result = 'ABORTED'
			error("'${colour}' no es un color de deploy valido, debe ser uno de ['blue', 'green']")
		}
		echo VS_MAP.toString()
	}
	sh """
		helm upgrade --install ${releaseName}-istio --namespace ${namespace} \
		--reuse-values --set istio.blueWeight=${VS_MAP["blue"]} \
		--set istio.greenWeight=${VS_MAP["green"]} --set istio.blueGreen=true --atomic
		kubectl scale --replicas=0 statefulset/${statefulsetName}-${DOWN_COLOR} -n ${namespace}
	"""
}
