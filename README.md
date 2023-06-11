# LogJerry

LogJerry is a desktop investigation tool for android logs.

Instead of relying on extensive editors like VS Code, LogJerry simplifies log analysis by offering easy-to-navigate columns and log filtering.
his tool is particularly handy for quickly locating information like JSON in your logs. 
Born out of a personal need for a better log investigation tool, we welcome all feedback as we continue to develop and improve LogJerry.

## Main features

- Auto-detect exceptions, JSON objects
- Prettify JSON
- Filters
- Find keywords in logs
- Make a note for each log

![logjerry_temp_720](https://user-images.githubusercontent.com/5154440/192139287-c049b3f1-9a6e-49f9-a15b-6817ef51a2ee.gif)
    
### Download

Only Mac ARM64 package is provided in the Release tab. 
You can download a .dmg file in the [releases](https://github.com/jkj8790/LogJerry/releases).

### Build

The other platforms except Mac ARM64, you can build an application that runs on your platform by yourself.
1. Clone this repository
2. Execute `createReleaseDistributable` gradle task. Run command `./gradlew createReleaseDistributable` on the project top directory 
3. Check `build/compose/binaries/main/app` folder

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
