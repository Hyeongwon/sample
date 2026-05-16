pipeline {
    agent {
        kubernetes {
            yaml '''
              apiVersion: v1
              kind: Pod
              spec:
                hostAliases:
                  - ip: "172.18.0.6"          # ← 위에서 확인한 kind-registry IP
                    hostnames:
                      - "kind-registry"
                containers:
                  - name: gradle
                    image: eclipse-temurin:17-jdk
                    command: ['cat']
                    tty: true
                  - name: kaniko
                    image: gcr.io/kaniko-project/executor:v1.22.0-debug
                    command: ['/busybox/cat']
                    tty: true
                  - name: git
                    image: alpine/git:latest
                    command: ['cat']
                    tty: true
            '''
        }
    }

    environment {
        REGISTRY     = 'kind-registry:5000'
        IMAGE_NAME   = 'grip-sample'
        // commit SHA의 짧은 버전을 태그로
        IMAGE_TAG    = "${env.GIT_COMMIT?.take(8) ?: 'manual'}"
        MANIFEST_REPO = 'https://github.com/Hyeongwon/manifests.git'
    }

    stages {
        stage('Build') {
            steps {
                    container('gradle') {
                        sh '''
                          chmod +x ./gradlew
                          ./gradlew bootJar --no-daemon
                        '''
                    }
                }
        }

        stage('Docker build & push') {
            steps {
                container('kaniko') {
                    sh '''
                      /kaniko/executor \
                        --context=`pwd` \
                        --dockerfile=Dockerfile \
                        --destination=${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} \
                        --insecure \
                        --skip-tls-verify
                    '''
                }
            }
        }

        stage('Update manifest') {
            steps {
                container('git') {
                    withCredentials([usernamePassword(
                        credentialsId: 'github-manifest-pat',
                        usernameVariable: 'GIT_USER',
                        passwordVariable: 'GIT_TOKEN'
                    )]) {
                        sh '''
                          set -e
                          git config --global user.email "jenkins@local"
                          git config --global user.name "Jenkins CI"

                          rm -rf manifest-repo
                          git clone https://${GIT_USER}:${GIT_TOKEN}@github.com/Hyeongwon/manifests.git manifest-repo
                          cd manifests/apps/grip-sample/overlays/dev

                          # kustomization.yaml의 newTag 줄을 새 태그로 치환
                          sed -i "s|newTag:.*|newTag: ${IMAGE_TAG}|" kustomization.yaml

                          git add kustomization.yaml
                          git commit -m "chore: bump sample dev image to ${IMAGE_TAG}"
                          git push origin main
                        '''
                    }
                }
            }
        }
    }
}