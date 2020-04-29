def call(String releaseName, String namespace, String colour, String chartLocation = './deploy/istio') {
	def BLUE_REPLICAS = 0
	def GREEN_REPLICAS = 0
	def COLOR_PARAM = ""
	def VS_MAP = ['blue': 0,
		'green': 0]
	script {
		if(colour != 'blue' || colour != 'green') {
			currentBuild.result = 'ABORTED'
			error("${colour} no es un color de deploy valido, debe ser uno de ['blue', 'green']")
		}
		VS_MAP[colour] = 100
	}
	sh """
		helm upgrade --install ${releaseName}-istio ${chartLocation} --reuse-values \
		--namespace ${namespace} --set istio.blueWeight=${VS_MAP['blue']} \
		--set istio.greenWeight=${VS_MAP['green']} --set istio.blueGreen=true
	"""
}
