## 存储服务V2版本API接口文档（K8s部署版）

### 基础信息

- **基础路径**: `/storage/api/v2`
- **版本**: V2
- **功能**: 提供统一的对象存储操作接口，支持多种存储后端
- **部署环境**: Kubernetes (KubeSphere)
- **服务名称**: `im-storage-svc`

### 服务访问信息

#### 生产环境访问方式

```bash
# 通过NodePort访问（推荐）
http://<任意节点IP>:31556/storage/api/v2/
# 通过域名
http://intelligentmfg.qtech.com/:31556/storage/api/v2/
```

#### 开发阶段访问方式

```bash
# 通过DNS域名访问（K8s环境）
http://im-storage-svc.qtech-im-api:8080/storage/api/v2/
```

#### 示例访问方式

```bash
# http://intelligentmfg.qtech.com:31556/storage/api/v2/objects/lst-doc-validation/Extreme%20host%20manual.pdf/metadata?storageType=ceph-s3
```

```bash
{
    "code": 200,
    "message": "Success",
    "data": {
        "bucketName": "lst-doc-validation",
        "objectKey": "Extreme host manual.pdf",
        "size": 2823326,
        "contentType": "application/pdf",
        "etag": "\"4fdfdeaec8c8eb16f022da06b7fee2d7\"",
        "lastModified": "2025-11-11T16:42:55",
        "storageClass": null
    },
    "timestamp": "2025-11-12T11:00:37.293"
}
```

```bash
http://intelligentmfg.qtech.com:31556/storage/api/v2/objects/lst-doc-validation
```

```bash
{
    "code": 200,
    "message": "Success",
    "data": [
        {
            "key": "Extreme host manual.pdf",
            "size": 2823326,
            "lastModified": "2025-11-11T16:42:55.29",
            "etag": "\"4fdfdeaec8c8eb16f022da06b7fee2d7\""
        },
        {
            "key": "test",
            "size": 2823326,
            "lastModified": "2025-11-11T16:27:57.907",
            "etag": "\"4fdfdeaec8c8eb16f022da06b7fee2d7\""
        },
        {
            "key": "画胶图像采集程序使用.pptx",
            "size": 1777256,
            "lastModified": "2025-11-11T16:42:08.025",
            "etag": "\"3fa83f7f4b912fd7d0cc8b8701f994ea\""
        }
    ],
    "timestamp": "2025-11-12T11:03:56.903"
}
```

```bash
http://intelligentmfg.qtech.com:31556/storage/api/v2/buckets/lst-doc-validation/exists
```

```bash
{
    "code": 200,
    "message": "Success",
    "data": true,
    "timestamp": "2025-11-12T11:05:14.418"
}
```

```bash
http://intelligentmfg.qtech.com:31556/storage/api/v2/objects/lst-doc-validation/Extreme%20host%20manual.pdf/metadata
```

```bash
{
    "code": 200,
    "message": "Success",
    "data": {
        "bucketName": "lst-doc-validation",
        "objectKey": "Extreme host manual.pdf",
        "size": 2823326,
        "contentType": "application/pdf",
        "etag": "\"4fdfdeaec8c8eb16f022da06b7fee2d7\"",
        "lastModified": "2025-11-11T16:42:55",
        "storageClass": null
    },
    "timestamp": "2025-11-12T11:12:33.703"
}
```

### 接口列表

1. **上传对象**
    - **URL**: `POST /objects`

2. **下载对象**
    - **URL**: `GET /objects/{bucketName}/{objectKey}`

3. **删除对象**
    - **URL**: `DELETE /objects/{bucketName}/{objectKey}`

4. **列出对象**
    - **URL**: `GET /objects/{bucketName}`

5. **获取对象元数据**
    - **URL**: `GET /objects/{bucketName}/{objectKey}/metadata`

6. **检查存储桶是否存在**
    - **URL**: `GET /buckets/{bucketName}/exists`

7. **创建存储桶**
    - **URL**: `POST /buckets/{bucketName}`

8. **删除存储桶**
    - **URL**: `DELETE /buckets/{bucketName}`

9. **生成预签名URL**
    - **URL**: `GET /presigned-url`

---

### 1. 上传对象

#### 接口描述

上传文件对象到指定存储桶

#### 请求信息

- **URL**: `POST /objects`
- **Content-Type**: `multipart/form-data`

#### 请求参数

| 参数名                                                                                                                                   | 类型            | 必填 | 说明                   |
|---------------------------------------------------------------------------------------------------------------------------------------|---------------|----|----------------------|
| [bucketName](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L14-L14)    | String        | 是  | 存储桶名称                |
| [objectKey](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L15-L15)     | String        | 是  | 对象键（文件路径）            |
| [file](file://E:\dossier\others\im-framework\storage-svc\Dockerfile)                                                                  | MultipartFile | 是  | 上传的文件                |
| [storageType](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\GetObjectRequest.java#L21-L21) | String        | 否  | 存储类型（如ceph-s3、hdfs等） |

#### 响应示例

```json
{
  "code": 200,
  "message": "Object uploaded successfully",
  "data": null,
  "timestamp": "2025-11-07T10:30:00"
}
```

---

### 2. 下载对象

#### 接口描述

从指定存储桶下载对象

#### 请求信息

- **URL**: `GET /objects/{bucketName}/{objectKey}`
- **Accept**: `application/octet-stream`

#### 路径参数

| 参数名                                                                                                                                | 类型     | 必填 | 说明    |
|------------------------------------------------------------------------------------------------------------------------------------|--------|----|-------|
| [bucketName](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L14-L14) | String | 是  | 存储桶名称 |
| [objectKey](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L15-L15)  | String | 是  | 对象键   |

#### 查询参数

| 参数名                                                                                                                                   | 类型     | 必填 | 说明   |
|---------------------------------------------------------------------------------------------------------------------------------------|--------|----|------|
| [storageType](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\GetObjectRequest.java#L21-L21) | String | 否  | 存储类型 |

#### 响应

返回文件二进制流，浏览器会提示下载

---

### 3. 删除对象

#### 接口描述

删除指定存储桶中的对象

#### 请求信息

- **URL**: `DELETE /objects/{bucketName}/{objectKey}`

#### 路径参数

| 参数名                                                                                                                                | 类型     | 必填 | 说明    |
|------------------------------------------------------------------------------------------------------------------------------------|--------|----|-------|
| [bucketName](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L14-L14) | String | 是  | 存储桶名称 |
| [objectKey](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L15-L15)  | String | 是  | 对象键   |

#### 查询参数

| 参数名                                                                                                                                   | 类型     | 必填 | 说明   |
|---------------------------------------------------------------------------------------------------------------------------------------|--------|----|------|
| [storageType](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\GetObjectRequest.java#L21-L21) | String | 否  | 存储类型 |

#### 响应示例

```json
{
  "code": 200,
  "message": "Object deleted successfully",
  "data": null,
  "timestamp": "2025-11-07T10:30:00"
}
```

---

### 4. 列出对象

#### 接口描述

列出存储桶中的对象

#### 请求信息

- **URL**: `GET /objects/{bucketName}`

#### 路径参数

| 参数名                                                                                                                                | 类型     | 必填 | 说明    |
|------------------------------------------------------------------------------------------------------------------------------------|--------|----|-------|
| [bucketName](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L14-L14) | String | 是  | 存储桶名称 |

#### 查询参数

| 参数名                                                                                                                                   | 类型     | 必填 | 说明      |
|---------------------------------------------------------------------------------------------------------------------------------------|--------|----|---------|
| [prefix](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ListObjectsRequest.java#L19-L19)    | String | 否  | 对象键前缀过滤 |
| [storageType](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\GetObjectRequest.java#L21-L21) | String | 否  | 存储类型    |

#### 响应示例

```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "key": "folder/file1.txt",
      "size": 1024,
      "lastModified": "2025-11-07T10:00:00",
      "etag": "abc123"
    }
  ],
  "timestamp": "2025-11-07T10:30:00"
}
```

---

### 5. 获取对象元数据

#### 接口描述

获取指定对象的元数据信息

#### 请求信息

- **URL**: `GET /objects/{bucketName}/{objectKey}/metadata`

#### 路径参数

| 参数名                                                                                                                                | 类型     | 必填 | 说明    |
|------------------------------------------------------------------------------------------------------------------------------------|--------|----|-------|
| [bucketName](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L14-L14) | String | 是  | 存储桶名称 |
| [objectKey](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L15-L15)  | String | 是  | 对象键   |

#### 查询参数

| 参数名                                                                                                                                   | 类型     | 必填 | 说明   |
|---------------------------------------------------------------------------------------------------------------------------------------|--------|----|------|
| [storageType](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\GetObjectRequest.java#L21-L21) | String | 否  | 存储类型 |

#### 响应示例

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "bucketName": "default",
    "objectKey": "test/file.txt",
    "size": 2048,
    "contentType": "text/plain",
    "etag": "def456",
    "lastModified": "2025-11-07T10:15:00",
    "storageClass": "STANDARD"
  },
  "timestamp": "2025-11-07T10:30:00"
}
```

---

### 6. 检查存储桶是否存在

#### 接口描述

检查指定存储桶是否存在

#### 请求信息

- **URL**: `GET /buckets/{bucketName}/exists`

#### 路径参数

| 参数名                                                                                                                                | 类型     | 必填 | 说明    |
|------------------------------------------------------------------------------------------------------------------------------------|--------|----|-------|
| [bucketName](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L14-L14) | String | 是  | 存储桶名称 |

#### 查询参数

| 参数名                                                                                                                                   | 类型     | 必填 | 说明   |
|---------------------------------------------------------------------------------------------------------------------------------------|--------|----|------|
| [storageType](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\GetObjectRequest.java#L21-L21) | String | 否  | 存储类型 |

#### 响应示例

```json
{
  "code": 200,
  "message": "Success",
  "data": true,
  "timestamp": "2025-11-07T10:30:00"
}
```

---

### 7. 创建存储桶

#### 接口描述

创建新的存储桶

#### 请求信息

- **URL**: `POST /buckets/{bucketName}`

#### 路径参数

| 参数名                                                                                                                                | 类型     | 必填 | 说明    |
|------------------------------------------------------------------------------------------------------------------------------------|--------|----|-------|
| [bucketName](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L14-L14) | String | 是  | 存储桶名称 |

#### 查询参数

| 参数名                                                                                                                                   | 类型     | 必填 | 说明   |
|---------------------------------------------------------------------------------------------------------------------------------------|--------|----|------|
| [storageType](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\GetObjectRequest.java#L21-L21) | String | 否  | 存储类型 |

#### 响应示例

```json
{
  "code": 200,
  "message": "Bucket created successfully",
  "data": null,
  "timestamp": "2025-11-07T10:30:00"
}
```

---

### 8. 删除存储桶

#### 接口描述

删除指定存储桶

#### 请求信息

- **URL**: `DELETE /buckets/{bucketName}`

#### 路径参数

| 参数名                                                                                                                                | 类型     | 必填 | 说明    |
|------------------------------------------------------------------------------------------------------------------------------------|--------|----|-------|
| [bucketName](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L14-L14) | String | 是  | 存储桶名称 |

#### 查询参数

| 参数名                                                                                                                                   | 类型     | 必填 | 说明   |
|---------------------------------------------------------------------------------------------------------------------------------------|--------|----|------|
| [storageType](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\GetObjectRequest.java#L21-L21) | String | 否  | 存储类型 |

#### 响应示例

```json
{
  "code": 200,
  "message": "Bucket deleted successfully",
  "data": null,
  "timestamp": "2025-11-07T10:30:00"
}
```

---

### 9. 生成预签名URL

#### 接口描述

生成预签名的上传或下载URL，用于临时访问对象

#### 请求信息

- **URL**: `GET /presigned-url`

#### 查询参数

| 参数名                                                                                                                                   | 类型     | 必填 | 说明                       |
|---------------------------------------------------------------------------------------------------------------------------------------|--------|----|--------------------------|
| [bucketName](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L14-L14)    | String | 是  | 存储桶名称                    |
| [objectKey](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\ObjectMetadata.java#L15-L15)     | String | 是  | 对象键                      |
| `urlType`                                                                                                                             | String | 是  | URL类型（UPLOAD 或 DOWNLOAD） |
| [storageType](file://E:\dossier\others\im-framework\storage-svc\src\main\java\com\im\storage\model\dto\GetObjectRequest.java#L21-L21) | String | 否  | 存储类型                     |
| `expirationSeconds`                                                                                                                   | Long   | 否  | 过期时间（秒），默认3600秒          |

#### 响应示例

```json
{
  "code": 200,
  "message": "Success",
  "data": "https://s3.amazonaws.com/bucket/object?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
  "timestamp": "2025-11-07T10:30:00"
}
```

---

### 支持的存储类型

- `ceph-s3`: Ceph S3存储
- `hdfs`: HDFS存储
- `minio`: MinIO存储
- `aliyun-oss`: 阿里云OSS
- `amazon-s3`: Amazon S3

### 错误响应格式

所有接口在出错时返回统一格式：

```json
{
  "code": 500,
  "message": "错误描述信息",
  "data": null,
  "timestamp": "2025-11-07T10:30:00"
}
```
