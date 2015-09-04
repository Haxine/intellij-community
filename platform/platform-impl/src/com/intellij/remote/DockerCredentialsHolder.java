/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.remote;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexander Koshevoy
 */
public class DockerCredentialsHolder {
  public static final String DOCKER_SERVER_NAME = "DOCKER_SERVER_NAME";
  public static final String DOCKER_IMAGE_NAME = "DOCKER_IMAGE_NAME";
  public static final String DOCKER_CONTAINER_NAME = "DOCKER_CONTAINER_NAME";
  public static final String DOCKER_REMOTE_PROJECT_PATH = "DOCKER_REMOTE_PROJECT_PATH";

  // TODO [Docker] do not store docker server itself but store id or special reference
  private DockerSupport.DockerServer myDockerServer;

  private String myImageName;

  private String myContainerName;

  private String myRemoteProjectPath;

  public DockerCredentialsHolder(DockerSupport.DockerServer server, String imageName, String containerName, String remoteProjectPath) {
    myDockerServer = server;
    myImageName = imageName;
    myContainerName = containerName;
    myRemoteProjectPath = remoteProjectPath;
  }

  public DockerSupport.DockerServer getDockerServer() {
    return myDockerServer;
  }

  public String getImageName() {
    return myImageName;
  }

  public String getContainerName() {
    return myContainerName;
  }

  public String getRemoteProjectPath() {
    return myRemoteProjectPath;
  }

  public void save(@NotNull Element element) {
    element.setAttribute(DOCKER_IMAGE_NAME, myImageName);
    element.setAttribute(DOCKER_CONTAINER_NAME, myContainerName);
    element.setAttribute(DOCKER_REMOTE_PROJECT_PATH, myRemoteProjectPath);
    // TODO [Docker] use better Docker server id than its name
    element.setAttribute(DOCKER_SERVER_NAME, myDockerServer.getName());
  }

  public void load(@NotNull Element element) {
    myImageName = element.getAttributeValue(DOCKER_IMAGE_NAME);
    myContainerName = element.getAttributeValue(DOCKER_CONTAINER_NAME);
    myRemoteProjectPath = element.getAttributeValue(DOCKER_REMOTE_PROJECT_PATH);
    DockerSupport instance = DockerSupport.getInstance();
    if (instance != null) {
      myDockerServer = instance.findDockerServerByName(element.getAttributeValue(DOCKER_SERVER_NAME));
    }
  }

}
