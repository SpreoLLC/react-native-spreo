import { NativeModules } from 'react-native';

//(NativeModules)  Define lazy getters for each module. These will return the module if already loaded, or load it if not.
const { Spreo, DownloadManager } = NativeModules;

export {Spreo,DownloadManager}
