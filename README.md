
# react-native-ftp

## Tested
Currently tested to work on Android

## Getting started

`$ npm install react-native-ftps --save`

### Mostly automatic installation

`$ react-native link react-native-ftp`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-ftp` and add `RNFtp.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNFtp.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNFtpPackage;` to the imports at the top of the file
  - Add `new RNFtpPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-ftp'
  	project(':react-native-ftp').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-ftp/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-ftp')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNFtp.sln` in `node_modules/react-native-ftp/windows/RNFtp.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Com.Reactlibrary.RNFtp;` to the usings at the top of the file
  - Add `new RNFtpPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript

import FTP from 'react-native-ftps';

const testFTP = async () =>
{
  const host = "HOST";
  const port = 990;
  const tls = true; //TLS goes with 990 true always
  
  //check for active connection
  let connected = await FTP.client_status().catch((error) => {return false});

  if( connected == "connected" )
  await logout(); //Close previous connection

  console.log('Make new connection'); 
  FTP.setup(host, port, tls ) 
  FTP.login("demo","password") 
  .then(async (result) => 
  {
    console.log(result, await FTP.client_status()); 
    FTP.list("./").then(
        (result)=>
        {
          console.log('Listing', result);
          setTimeout( async () => {
            await logout(); 
          }, 2000);
        })
        .catch((e) => {
        console.log('ERR', e)
      });
  },
  (error) => 
  {
    console.log(error);
  })
  .catch(async (error) => 
  {
	console.log('Catch', error);
  })
}

const disconnectFTP = async () => {
  let connected = await FTP.client_status().catch((error) => {return false});

  if(connected == "connected")
  await logout(); 
  // else
  // FTP.force_disconnect();
}
