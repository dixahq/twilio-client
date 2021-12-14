# What is this?

This is a basic template we can use to create repos for new scala services.

# How do i use it?

Let's assume your new scala service is called `functionality`, use a noun for the service name, singular or plural.
There is no need to append the `-service` name.

## Configure Jenkins CI build

1. Use the green button "Use this template". This will let you create a new service repository using this template.

2. Create an AWS ECR repository called `functionality`. There is no need to configure any ECR settings.

3. Configure the github repository settings as described at [Creating new Github repositories](https://www.notion.so/dixa/Creating-new-Github-repositories-0ab327e732a744958d0bd1654be082bc).

4. In your first pull request replace the ECR repository name provided as the argument to `initializeBuild` with `functionality`
   https://github.com/dixahq/scala-service-template/blob/master/Jenkinsfile#L4

5. Make the service bootable and runnable, just doing nothing.

## Deploy the service onto k8s

1. Take a recent service example from the `staging-2` namespace as a template, and replace the occurrences
   of the service name with your `functionality`

2. Make sure the pod spec uses `livenessProbe` based on thrift tcpSocket connection, if possible, and `readinessProbe` based on prometheus httpGet request.

3. Initially use `cpu: limits: 1000m` (limit to maximum of 1 cpu core) to prevent unbound cpu usage by your new service.

4. Monitor the resources usage, tune the resources according to https://github.com/dixahq/wiki/wiki/Infrastructure-pod-resource-tuning.
   Remove the `cpu: limits` after the service seems stable.

5. Deploy your service to other staging namespaces, then to production, and tune the production resources.

## Configure Scala Steward updates

All new scala services **MUST** be configured to start receiving dependency updates from _scala steward_, please have a look at [the scala steward readme.md](https://github.com/dixahq/scala-steward) for further instructions.

