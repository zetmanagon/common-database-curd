# 结构及流程
![image](https://raw.githubusercontent.com/zetmanagon/common-database-curd/master/images/%E6%9C%8D%E5%8A%A1%E7%BB%93%E6%9E%84.png)

# mysql微服务框架
common-db-core、common-db-lib

## 框架依赖
mysql访问使用jpa

web服务使用springboot

远程调用使用feign

api说明文档使用swagger
## 主要代码

| 类名 | 功能 | 
|---|---
|com.gemantic.db.model.BaseModel|建表必存在的字段，所有model的基类|
|com.gemantic.db.client.BaseDBClient<T>|客户端调用代码，所有client的基础接口|
|com.gemantic.db.repository.BaseRepository<T,ID>|服务端请求mysql仓库接口，所有repository的基础接口|
|com.gemantic.db.controller.BaseController<T extends BaseModel>|服务端提供的web服务，所有的controller的基类|

## 规范
所有的model都必须有的字段

|实体属性名|	属性类型|	数据库字段名|	字段类型|	说明|
|---|---
|id|Long|	id|bigint|主键|
|createAt|Long	|create_at	|bigint	|创建时间|
|updateAt|Long|update_at|bigint|更新时间|
## 例子
task表为例
### 建表语句
```Sql
CREATE TABLE `task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `task_type` varchar(500) NOT NULL,
  `name` char(32) NOT NULL,
  `task_desc` varchar(500) NOT NULL,
  `copy_task_id` bigint(20) DEFAULT NULL COMMENT '复制的任务ID',
  `create_at` bigint(20) NOT NULL,
  `update_at` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT
```
### core编写
程序包含：model(orm对象与表的映射) ,client(客户端调用服务端代码),其他自定义工具类
#### pom依赖
```Xml
<parent>
<groupId>com.gemantic.microservices</groupId>
<artifactId>spring-boot-gemantic-core-parent</artifactId>
<version>2.1.6</version>
</parent>

....
<dependency>
   <groupId>com.gemantic.microservices</groupId>
   <artifactId>common-db-core</artifactId>
   <version>0.6</version>
</dependency>
<dependency>
   <groupId>org.projectlombok</groupId>
   <artifactId>lombok</artifactId>
   <optional>true</optional>
</dependency>
```
#### model定义
```Java
package com.gemantic.ai.model;

import com.gemantic.db.model.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;

@ApiModel("任务表")
@Entity
@Data
@ToString
@EqualsAndHashCode
public class Task extends BaseModel {

    private static final long serialVersionUID = -7694862215213568642L;

    @ApiModelProperty(value = "用户编号")
    private Long userId;

    @ApiModelProperty(value = "任务类别:DIALOGUE-对话;TAGGING-标注",allowableValues = "DIALOGUE,TAGGING")
    private String taskType;

    @ApiModelProperty(value = "任务名")
    private String name;

    @ApiModelProperty(value = "任务描述")
    private String taskDesc = "";

    @ApiModelProperty(value = "复制的任务ID")
    private Long copyTaskId = 0L;

}

```
#### client
```Java
import com.gemantic.ai.model.Task;
import com.gemantic.db.client.BaseDBClient;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "TaskClient", url = "${ai.db.service.url}",path = "task")
public interface TaskClient extends BaseDBClient<Task> {
}

```
### service
程序包含：提供web请求的controller,访问mysql的repository

#### pom依赖
```Xml
<parent>
    <groupId>com.gemantic.microservices</groupId>
    <artifactId>spring-boot-gemantic-service-parent</artifactId>
    <version>2.1.6</version>
</parent>
...


<dependency>
    <groupId>com.gemantic.microservices</groupId>
    <artifactId>common-db-lib</artifactId>
    <version>0.7</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.41</version>
</dependency>

<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>${druid.version}</version>
</dependency>
```
#### 数据库请求地址application.properties配置
```Properties
spring.jpa.show-sql=true
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.druid.initial-size=2
spring.datasource.druid.max-active=200
spring.datasource.druid.min-idle=0
spring.datasource.druid.max-wait=60000
spring.datasource.druid.keep-alive=true
spring.datasource.druid.validation-query=SELECT 1
spring.datasource.druid.test-on-borrow=false
spring.datasource.druid.test-on-return=false
spring.datasource.druid.test-while-idle=true
spring.datasource.username=yourUserName
spring.datasource.password=yourPassword
spring.datasource.url=jdbc:mysql://10.0.0.20:3306/ai?useUnicode=true&characterEncoding=utf8
```
#### server
注意：必须配置BaseRepositoryImpl替代jpa的实现类SimpleJpaRepository
```Java
package com.gemantic;

import com.gemantic.db.repository.impl.BaseRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 配置bean
 *
 * @author yhye 2016年9月20日下午4:55:17
 */
@SpringBootApplication
@EnableJpaRepositories(value = "com.gemantic.ai",repositoryBaseClass= BaseRepositoryImpl.class)
@EnableAsync
public class Server {

  public static void main(String[] args) {

    SpringApplication.run(Server.class,args);
  }
}

```

#### repository
```Java
package com.gemantic.ai.repository;

import com.gemantic.ai.model.Task;
import com.gemantic.db.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends BaseRepository<Task,Long> {


}

```
#### controller
```Java
package com.gemantic.ai.controller;

import com.gemantic.ai.client.TaskClient;
import com.gemantic.ai.model.Task;
import com.gemantic.ai.repository.TaskRepository;
import com.gemantic.db.controller.BaseController;
import com.gemantic.db.repository.BaseRepository;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(path = "/task")
@Api(description = "任务表")
public class TaskController extends BaseController<Task> implements TaskClient {

    @Resource
    private TaskRepository taskRepository;

    @Override
    public BaseRepository<Task,Long> getRepository() {
        return this.taskRepository;
    }

    @Override
    public List<String> getOnlyInsertField() {
        return DEFAULT_ONLY_INSERT_FIELD;
    }
}
```
### 服务提供
ok，服务启动后查看swagger,我们看看都提供了哪些服务？
![image](https://raw.githubusercontent.com/zetmanagon/common-database-curd/master/images/mysql.png)

## 客户端调用
### application.properties配置
```Properties
ai.db.service.url=http://ai-db-service:31001
```
### server
```Java
package com.gemantic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 配置bean
 *
 * @author yhye 2016年9月20日下午4:55:17
 */
@SpringBootApplication
@EnableAsync
@EnableFeignClients
public class Server {

  public static void main(String[] args) {

    SpringApplication.run(Server.class,args);
  }
}

```
### http配置
```Java
package com.gemantic.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.alibaba.fastjson.support.spring.JSONPResponseBodyAdvice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

/**
 * 容器配置
 * 
 * @author yhye 2016年9月20日下午4:55:17
 */
@Configuration
public class HttpConfig {

   /**
    * jsonp,按照url定义是否支持jsonp,如果需要支持,则在方法上面添加注释@ResponseJSONP
    * @return jonsp Response 对象
    */
   public @Bean
   JSONPResponseBodyAdvice jsonpResponseBodyAdvice() {
      return new JSONPResponseBodyAdvice();
   }

   public @Bean
   FastJsonHttpMessageConverter fastJsonpHttpMessageConverter() {
   FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
      FastJsonConfig fastJsonConfig = new FastJsonConfig();

      fastJsonConfig.setSerializerFeatures(
            SerializerFeature.DisableCircularReferenceDetect,
            SerializerFeature.BrowserSecure);
      converter.setFastJsonConfig(fastJsonConfig);

      List<MediaType> supportedMediaTypes = new ArrayList<>();
      supportedMediaTypes.add(MediaType.APPLICATION_JSON);
      supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
      supportedMediaTypes.add(MediaType.APPLICATION_ATOM_XML);
      supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
      supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
      supportedMediaTypes.add(MediaType.APPLICATION_PDF);

      supportedMediaTypes.add(MediaType.APPLICATION_XHTML_XML);
      supportedMediaTypes.add(MediaType.APPLICATION_XML);
      supportedMediaTypes.add(MediaType.IMAGE_GIF);
      supportedMediaTypes.add(MediaType.IMAGE_JPEG);
      supportedMediaTypes.add(MediaType.IMAGE_PNG);

      supportedMediaTypes.add(MediaType.TEXT_HTML);
      supportedMediaTypes.add(MediaType.TEXT_MARKDOWN);
      supportedMediaTypes.add(MediaType.TEXT_PLAIN);
      supportedMediaTypes.add(MediaType.TEXT_XML);
      converter.setSupportedMediaTypes(supportedMediaTypes);

      return converter;
   }

   
}
```

### 接口使用
```Java
@Resource
private TaskClient taskClient;


...


Map<String,String> params = Maps.newHashMap();
params.put("userId",userId);
params.put("taskType",taskType);
return taskClient.find(ids, null, null, null, Lists.newArrayList(orderBy), Lists.newArrayList(direction),
        likeFields, likes,null, cp, ps, params,null,null);

```

# mongodb微服务框架
common-doc-core、common-doc-lib

## 框架依赖
mongodb访问使用mongoTemplate

web服务使用springboot

远程调用使用feign

api说明文档使用swagger


# elasticsearch7微服务框架
common-search-core、common-search-lib
## 框架依赖
Elasticsearch使用es官方提供的客户端

web服务使用springboot

远程调用使用feign

api说明文档使用swagger
