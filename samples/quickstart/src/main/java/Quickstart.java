/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple Quickstart application showing how to use Shiro's API.
 *
 * @since 0.9 RC2
 */
public class Quickstart {

    private static final transient Logger log = LoggerFactory.getLogger(Quickstart.class);


    public static void  main(String[] args) {

        //使用抽象工厂模式，获取SecurityManager工厂，根据shiro.ini文件
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        //初始化SecurityManager，并获得SecurityManager实例
        SecurityManager securityManager = factory.getInstance();
        //将SecurityManager实例，绑定给SecurityUtils
        SecurityUtils.setSecurityManager(securityManager);

        //获得 Subject 对象,shiro的所有操作(验证 授权)API，都是通过Subject调用完成的
        //当前这个用户 一般情况指的是数据库里面一条用户数据
        Subject currentUser = SecurityUtils.getSubject();

        //获得shiro的session，shiro的session可以通过可以使用httprequest 获取及管理
        Session session = currentUser.getSession();
        // 测试为session添加 key value
        session.setAttribute("someKey", "aValue");
        // 测试获取session中的内容
        String value = (String) session.getAttribute("someKey");
        if (value.equals("aValue")) {
            log.info("Retrieved the correct value! [" + value + "]");
        }


        // Subject.isAuthenticated 判断当前用户是否已经登录；true 已经登录，false 没有登录；
        // 根据用户不同登录状态，执行不同的逻辑
        if (!currentUser.isAuthenticated()) {

            //根据用户输入的用户名和密码 生成token,token 也可使用其它信息生产 例如邮件 手机号
            UsernamePasswordToken token = new UsernamePasswordToken("lonestarr", "vespa");
            // 记住我
            token.setRememberMe(true);
            try {
                //当前用户验证
                //如果登录失败 将会返回不同类型的异常
                currentUser.login(token);
            } //用户名错误
            catch (UnknownAccountException uae) {
                log.info("There is no user with username of " + token.getPrincipal());
            }// 密码错误
            catch (IncorrectCredentialsException ice) {
                log.info("Password for account " + token.getPrincipal() + " was incorrect!");
            }//用户被锁
            catch (LockedAccountException lae) {
                log.info("The account for username " + token.getPrincipal() + " is locked.  " +
                        "Please contact your administrator to unlock it.");
            }
            // ... catch more exceptions here (maybe custom ones specific to your application?
            catch (AuthenticationException ae) {
                //unexpected condition?  error?
            }
        }

        //say who they are:
        //print their identifying principal (in this case, a username):
        log.info("User [" + currentUser.getPrincipal() + "] logged in successfully.");

        //test a role:
        if (currentUser.hasRole("schwartz")) {
            log.info("May the Schwartz be with you!");
        } else {
            log.info("Hello, mere mortal.");
        }

        //test a typed permission (not instance-level)
        if (currentUser.isPermitted("lightsaber:wield")) {
            log.info("You may use a lightsaber ring.  Use it wisely.");
        } else {
            log.info("Sorry, lightsaber rings are for schwartz masters only.");
        }

        //a (very powerful) Instance Level permission:
        if (currentUser.isPermitted("winnebago:drive:eagle5")) {
            log.info("You are permitted to 'drive' the winnebago with license plate (id) 'eagle5'.  " +
                    "Here are the keys - have fun!");
        } else {
            log.info("Sorry, you aren't allowed to drive the 'eagle5' winnebago!");
        }

        //all done - log out!
        currentUser.logout();

        System.exit(0);
    }
}
