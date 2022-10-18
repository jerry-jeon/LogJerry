# LogJerry

LogJerry is a desktop investigation tool for android logs.

It's useful when you should take a at the logs without logcat. It will help you to find information such as JSON easily in the log.  
I frequently investigate android logs and I found there's no proper tool for doing it.
Editors like VS Code, it's hard to see the logs because I can see all the columns and I can't filter out logs.  
So that's why I am making this project. Any feedbacks are welcome!

## Main features

- Filters
- Find keywords in logs
- Toggle columns
- Auto-detect exceptions, JSON objects
- Prettify JSON

![logjerry_temp_720](https://user-images.githubusercontent.com/5154440/192139287-c049b3f1-9a6e-49f9-a15b-6817ef51a2ee.gif)
    
### Download

Only Mac ARM64 package is provided in the Release tab. 
You can download a .dmg file in the [releases](https://github.com/jkj8790/LogJerry/releases).

### Build

The other platforms except Mac ARM64, you can build an application that runs on your platform by yourself.
1. Clone this repository
2. Execute `createDistributable` gradle task. `./gradlew createDistributable`
3. Check `build/compose/binaries/main/app` folder

### Next features

- More color configurations
- Easy scroll like VS code Map Mode
- Merge cells
- Make possible to change the column size
- Make font size configurable
- Support showing ADB logcat buffer

# License
```
   Copyright 2022 KyoungJoo Jeon

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
