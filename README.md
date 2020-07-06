# realestate-jenkins-library

## Deployment frequency

- [ ] Agregar en jenkins este repositorio como libreria en Global Pipeline Libraries con el boton Add

Campo | Valor
------|---------
Name | realestate-jenkins-library
Default version | master
Load implicitly | tick
Allow default version to be overridden | tick
Include @Library changes in job recent change | tick
Retrieval method | Legacy SCM
Source Code Management | Git
Repository URL | git@github.com:ITNavent/realestate-jenkins-library.git
Credentials | corerealestate
Branches to build | */master

- [ ] En jenkins instalar el plugin https://plugins.jenkins.io/influxdb/
- [ ] Configurar el InfluxDB Targets

Campo | Valor
------|---------
Description | influxdb-redeoall
URL | http://influxdb-all.core.re.navent.biz:8086/
Username | admin
Database | deploy_metrics
Retention Policy | autogen
Job scheduled time as timestamp | tick
Expose Exceptions | tick

- [ ] En el job de jenkins productivo agregar al final:

```groovy
post {
		success {
			script {
				countDeploymentFrequency()
			}
		}
	}
```
