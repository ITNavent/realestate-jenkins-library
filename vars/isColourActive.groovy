def call(String vsName, String colour, String namespace) {
	def VS_MAP = ["blue": 0, "green": 0, "red": 0]
	script {
		try {
			def VS_JSON = sh(returnStdout: true, script: "kubectl get virtualservice ${vsName} -n ${namespace} -o json")
			def VS_PROPS = readJSON text: VS_JSON
			def VS_ROUTES = VS_PROPS.spec.http[0].route
			for(route in VS_ROUTES) {
				VS_MAP[route.destination.subset] = (route.weight ?: 0).toInteger()
			}
		} catch(err) {
			// no existe deploy de istio, arranco como de cero.
			VS_MAP["blue"] = 100
		}
		echo "isColourActive virtual service map ${VS_MAP.toString()}"
		return VS_MAP[colour] > 0;
	}
}