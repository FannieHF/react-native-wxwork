
# react-native-wxwork

## Getting started

`$ npm install react-native-wxwork --save`

### Mostly automatic installation

`$ react-native link react-native-wxwork`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-wxwork` and add `RNWxwork.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNWxwork.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNWxworkPackage;` to the imports at the top of the file
  - Add `new RNWxworkPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-wxwork'
  	project(':react-native-wxwork').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-wxwork/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-wxwork')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNWxwork.sln` in `node_modules/react-native-wxwork/windows/RNWxwork.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Wxwork.RNWxwork;` to the usings at the top of the file
  - Add `new RNWxworkPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNWxwork from 'react-native-wxwork';

// TODO: What to do with the module?
RNWxwork;
```
  