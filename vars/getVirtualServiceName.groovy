def call(String releaseName, String namespace) {
	script {
		try {
			def ISTIO_YAML = sh(returnStdout: true, script: "helm get manifest ${releaseName}-istio -n ${namespace}")
			def ISTIO_ENTITIES = readYaml text: ISTIO_YAML
			for(istio_entity in ISTIO_ENTITIES) {
				if(istio_entity.kind == "VirtualService") {
					return istio_entity.metadata.name
				}
			}
			ISTIO_ENTITIES = null
		} catch(err) {
			error("No se encontro el nombre del VirtualService en release ${releaseName}-istio")
		}
		error("No se encontro el nombre del VirtualService en release ${releaseName}-istio")
	}
}