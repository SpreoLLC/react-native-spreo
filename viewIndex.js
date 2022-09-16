import { requireNativeComponent,Platform } from 'react-native';

// (requireNativeComponent) used like React components which represent native view managers
const SpreoDualMapView = requireNativeComponent('SpreoDualMapView', null);
const MapBoxView = requireNativeComponent('Spreo', null);

export default Platform.OS == "ios" ?MapBoxView: SpreoDualMapView;