# LiteRelease-Maven-Plugin

## 1. Introduction

This plugin is a replacement of maven-release-plugin because the Apache's plugin is a heavyweight process, so it's indicate for a more simple (or not) projects.

## 2. Parameters

* development.messsage (required=false, defaultValue="[Next Development]")
* development.version (required=true)
* release.goal (required=false, defaultValue="deploy")
* release.messsage (required=false, defaultValue="[Release]")
* release.version (required=true)
* tag.name (required=true, defaultValue="${release.version}")

## 3. Example of Use

```
mvn br.org.otojunior:literelease-maven-plugin:1.3.0:release \
  -Ddevelopment.messsage=[Início Sprint-41] \
  -Ddevelopment.version=41.0.0-SNAPSHOT \
  -Drelease.messsage=[Release Sprint-40] \
  -Drelease.version=40.0.0 \
  -Drelease-goal=deploy \
  -Dtag.name=v40.0.0
```
