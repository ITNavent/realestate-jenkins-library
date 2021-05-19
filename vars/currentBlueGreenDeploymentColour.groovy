def call(String vsName, String namespace) {
	def COLOR_PARAM = ""
	def VS_MAP = ["blue": 0, "green": 0]
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
		echo "virtual service map ${VS_MAP.toString()}"
	}
	script {
		ansiColor('xterm') {
            if(VS_MAP["blue"] == 100 && VS_MAP["green"] == 0) {
                COLOR_PARAM = 'blue'
            } else if(VS_MAP["blue"] == 0 && VS_MAP["green"] == 100) {
                COLOR_PARAM = 'green'
            } else {
                currentBuild.result = 'ABORTED'
                error('No se pudo decidir a que color debería desplegarse.')
            }
        }
		return COLOR_PARAM;
	}
}