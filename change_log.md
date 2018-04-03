#### v1.2.0
**更新时间**   
2018-04-03

**Change Log**  
1、功能优化  
2、修复部份bug

#### v1.1.3  
**更新时间**   
2018-02-09  

**Change Log**  
修改兼容性问题  

#### v1.1.2  
**更新时间**   
2018-02-03  

**Change Log**  
1、配合业务需要修改  

**升级指南**  
重写WebChromeClient的openFileChooser、onShowFileChooser、onGeolocationPermissionsShowPrompt、onPermissionRequest四个方法，分别调用h5SDKHelper的相应方法，参考DemoActivity  

#### v1.1.0  
**更新时间**   
2017-12-14  

**Change Log**  
1、权根管理优化  
2、拍照优化(拍照实现可自定义)  
3、支付优化  

**升级指南**  
将h5bridge目录全部替换，并按原集成步骤操作  
1、H5SDKHelperr的onRequestPermissionsResult、onActivityResult方法已弃用，不需要在Activity/Fragment相应方法中调用了  
2、H5SDKHelper新增方法setCapture,可以设置拍照的实现，SDK中实现了两种SystemCaptureImpl(调用系统相机拍照)、CustomCaptureImpl(调用自定义相机拍照)  
   也可以自定义Capture的实现  
   
#### v1.0.1  
**更新时间**   
2017-10-30  

**Change Log**  
1、点页H5页面中电话号码由直接拔打电话改为跳转到拔号页面，给用户选择是否要拔打。  
2、取消不必要权限申请，提升用户体验。  

**升级指南**  
将h5bridge目录全部替换，并按原集成步骤操作  

#### v1.0.0 
**更新时间**  
2017-10-24  

**Change Log**  
初版发布