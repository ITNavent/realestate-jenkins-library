def call(String releaseName, String namespace, String colour, String chartLocation = './deploy/istio', String extraValues = "") {
	def BLUE_REPLICAS = 0
	def GREEN_REPLICAS = 0
	def COLOR_PARAM = ""
	def VS_MAP = ["blue": 0,
		"green": 0]
	script {
		if(colour != "blue" && colour != "green") {
			currentBuild.result = 'ABORTED'
			error("'${colour}' no es un color de deploy valido, debe ser uno de ['blue', 'green']")
		}
		VS_MAP[colour] = 100
		echo VS_MAP.toString()
	}
	sh """
		helm upgrade --install ${releaseName}-istio ${chartLocation} ${extraValues} \
		--namespace ${namespace} --set istio.blueWeight=${VS_MAP["blue"]} \
		--set istio.greenWeight=${VS_MAP["green"]} --set istio.blueGreen=true --atomic
	"""
}
