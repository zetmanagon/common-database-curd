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
