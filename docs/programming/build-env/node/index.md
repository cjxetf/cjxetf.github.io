# 搭建 Node 开发环境

## 1.Node 版本管理工具 NVM

NVM (Node Version Manager) 是一个用于管理多个 Node.js 版本的工具，可以方便地在不同项目之间切换 Node.js 版本。

1. 安装 NVM
   
   ```shell
   brew install nvm
   ```

2. 配置 NVM 环境变量，编辑`~/.zshrc`文件，添加以下内容：
   
   ```shell
   export NVM_DIR=~/.nvm
   export NVM_NODEJS_ORG_MIRROR=https://npmmirror.com/mirrors/node/
   source $(brew --prefix nvm)/nvm.sh
   ```

3. NVM 常用命令
   
    ```plaintext
    nvm ls ：列出所有已安装的 node 版本

    nvm ls-remote ：列出所有远程服务器的版本（官方node version list）

    nvm list ：列出所有已安装的 node 版本

    nvm list available ：显示所有可下载的版本

    nvm install stable ：安装最新版 node

    nvm install [node版本号] ：安装指定版本 node

    nvm uninstall [node版本号] ：删除已安装的指定版本

    nvm use [node版本号] ：切换到指定版本 node

    nvm current ：当前 node 版本

    nvm alias [别名] [node版本号] ：给不同的版本号添加别名

    nvm unalias [别名] ：删除已定义的别名

    nvm alias default [node版本号] ：设置默认版本
    ```

4. 安装多个版本 Node.js：

    ```shell
    nvm install 16
    nvm install 18
    nvm install 20

    // 设置默认版本
    nvm alias default 16
    ```

5. NVM 支持使用`.nvmrc`文件切换 Node 版本，这样可以在不同的项目中添加`.nvmrc`文件，进入项目后执行`nvm install`自动安装所需版本，又或者执行`nvm use`切换到指定版本，不过这样有点麻烦，好在`oh-my-zsh`支持`nvm`插件，可以实现**自动安装与切换**，编辑`~/.zshrc`，在`plugins`配置项中增加`nvm`，以及新增 3 行配置，如下：

    ```bash
    # 新增 nvm
    plugins=(nvm git ...)

    zstyle ':omz:plugins:nvm' lazy yes
    zstyle ':omz:plugins:nvm' autoload yes
    zstyle ':omz:plugins:nvm' silent-autoload yes
    ```

    编辑保存后，执行命令`source ~/.zshrc`使配置生效，测试下，在某个文件夹下新增`.nvmrc`文件，里面填写版本号，如：
   
    ```plaintext
    18
    ```
   
    进入此文件夹，执行`node -v`确认是否和文件中版本一致，注意：最好先设置一个默认的版本。

6. 安装好 Node 后，`npm`会自动安装，可以通过`npm -v`查看版本号，修改`npm`镜像源为淘宝镜像，加快下载速度：

    ```shell
    npm config set registry https://registry.npmmirror.com/
    ```

    查看当前镜像源：

    ```shell
    npm config get registry
    ```

7. 安装`yarn`包管理器：
   
   yarn 相较于 npm，有更快的安装速度和更好的离线支持。

    ```shell
    npm install -g yarn
    ```

    修改`yarn`镜像源为淘宝镜像：

    ```shell
    yarn config set registry https://registry.npmmirror.com/
    ```

    查看当前镜像源：

    ```shell
    yarn config get registry
    ```


