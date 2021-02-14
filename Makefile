.PHONY: run
run:
	git add *
	git commit -m "none"
	git push
	java -jar /Users/vinitkapoor/Learning/jenkins/jenkins-cli.jar -s http://localhost:8080/ -webSocket -auth admin:admin build new-pipeline
	terraform plan -var-file="test/testing.auto.tfvars"



