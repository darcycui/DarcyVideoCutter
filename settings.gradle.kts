pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        // 使用 repo1 maven 代替 mavenCentral, 注释mavenCentral()
        maven("https://repo1.maven.org/maven2") {
            // 禁用元数据重定向, 强制只从此仓库解析
            metadataSources {
                mavenPom()
                artifact()
                ignoreGradleMetadataRedirection()
            }
        }
        maven("https://maven.aliyun.com/repository/jcenter")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/gradle-plugin")

        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        maven("https://maven.aliyun.com/repository/public")
        // 使用 repo1 maven 代替 mavenCentral, 注释mavenCentral()
        maven("https://repo1.maven.org/maven2") {
            // 禁用元数据重定向, 强制只从此仓库解析
            metadataSources {
                mavenPom()
                artifact()
                ignoreGradleMetadataRedirection()
            }
        }
        maven("https://maven.aliyun.com/repository/jcenter")
        maven("https://maven.aliyun.com/repository/google")
        google()
        mavenCentral()
    }
}

rootProject.name = "DarcyVideoCutter"
include(":app")
include(":lib_saf_select")
include(":lib_media3_player")
include(":lib_log_toast")
