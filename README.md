# tmaps-android-sample

## Installation
### To integrate TMaps into your Android Studio project, add it to your project:
1. Create folder named tmapswww
2. Download and Unzip map package inside tmapswww (Request map package from Tagipedia Team)
3. Duplicate Config.Secrets.Example.xml and rename it to 'Config.Secrets.xml' and fill the file with your configration info
4. Read our sample for examples

## How it works
it works using dispatch actions between your APP and TMaps. So your APP dispatch actions to TMaps and TMaps dispatch actions to your APP.

## Usage
### <a name="RELOAD">Allow Reload map from JS</a> 

**IMPORTANT:** You should inject this script into webview because it is called from TMap when TMap need to reload webview. **if you don't inject it webview will not reloaded**.

```java
mWebView.addJavascriptInterface(new JavascriptInterface(){
      @JavascriptInterface
      public void reload() {
          runOnUiThread(new Runnable() { @Override public void run() { mWebView.reload(); } });
      }
},"__tmaps_bridge__");
```
```java
String tbString = "window.__reload__ = function(){__tmaps_bridge__.reload();};";
injectScript(tbString);
```

### TMaps actions dispatched to Your APP

#### <a name="READY">Ready</a>

dispatched when TMaps ready to receive dispatches from Your APP. So **you should not dispatch any action before TMaps get ready**.

```java
new HashMap<String, Object>(){{
   put("type", "READY");
}};
```

___

#### <a name="MAP_LOADED">Map loaded</a>

dispatched when map loaded and visible to user.

```java
new HashMap<String, Object>(){{
   put("type", "MAP_LOADED");
}};
```
___

#### Features tapped

dispatched when features in map tapped.

```java
new HashMap<String, Object>(){{
   put("type", "FEATURES_TAPPED");
   put("features", features);
}};
```

&nbsp;&nbsp;&nbsp;&nbsp;**features** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *List* with *Map*, each *Map* with *id*, *properties* keys

___

#### Associated feature tapped

dispatched when features in map tapped with the top feature visible to user.

```java
new HashMap<String, Object>(){{
   put("type", "ASSOCIATED_FEATURE_TAPPED");
   put("feature_id", feature_id);
   put("feature", feature);
}};
```

&nbsp;&nbsp;&nbsp;&nbsp;**feature_id** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *String* with valid feature id

&nbsp;&nbsp;&nbsp;&nbsp;**feature** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *Map* with *id*, *properties* keys

___

#### Category marked

dispatched when select category in map

```java
new HashMap<String, Object>(){{
   put("type", "CATEGORY_MARKED");
   put("category", category);
}};
```

&nbsp;&nbsp;&nbsp;&nbsp;**category** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Required** valid *String* category

___

#### Error

dispatched when error happened in TMaps

```java
new HashMap<String, Object>(){{
   put("type", "ERROR");
   put("error", error);
}};
```

&nbsp;&nbsp;&nbsp;&nbsp;**error** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *Map* with *stack* key and *String* value

___

#### Feature marked

dispatched after MARK_FEATURE ended

```java
new HashMap<String, Object>(){{
  put("type", "FEATURE_MARKED");
  put("feature_id", feature_id);
}};
```

&nbsp;&nbsp;&nbsp;&nbsp;**feature_id** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *String* with valid feature id

___

#### <a name="FEATURE_HIGHLIGHTED">Feature highlighted</a>

dispatched after HIGHLIGHT_FEATURE ended

```java
new HashMap<String, Object>(){{
  put("type", "FEATURE_HIGHLIGHTED");
  put("feature_id", feature_id);
}};
```

&nbsp;&nbsp;&nbsp;&nbsp;**feature_id** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *String* with valid feature id

___

#### <a name="ZOOM_ENDED">Zoom ended</a>

dispatched after SET_ZOOM ended

```java
new HashMap<String, Object>(){{
  put("type", "ZOOM_ENDED");
}};
```
___

#### <a name="CENTER_ENDED">Center ended</a>

dispatched after SET_CENTER ended

```java
new HashMap<String, Object>(){{
  put("type", "CENTER_ENDED");
}};
```

___

#### <a name="EVENT_LOGGED">Event logged</a>

dispatched after any event happened in TMaps. Your app can send analytics after receive this action.

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "Maps");
  put("event_action", "Loaded");
  put("event_label", map_name);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "Features");
  put("event_action", "Tapped");
  put("event_label", feature_display_name);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
      put("feature_id", feature_id);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "Features");
  put("event_action", "Highlighted");
  put("event_label", feature_display_name);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
      put("feature_id", feature_id);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "Features");
  put("event_action", "Searched");
  put("event_label", feature_display_name);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
      put("feature_id", feature_id);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "Categories");
  put("event_action", "Highlighted");
  put("event_label", category);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "Categories");
  put("event_action", "Searched");
  put("event_label", category);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "Routes");
  put("event_action", "Routed");
  put("event_label", source_display_name => target_display_name);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
      put("source_feature_id", source_feature_id);
      put("target_feature_id", target_feature_id);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "Buildings");
  put("event_action", "Tapped");
  put("event_label", building_name);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
      put("building_id", building_id);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "Buildings");
  put("event_action", "Opened");
  put("event_label", building_name);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
      put("building_id", building_id);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "Floors");
  put("event_action", "Opened");
  put("event_label", floor_label);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
      put("layer_id", layer_id);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "RenderModes");
  put("event_action", "Changed");
  put("event_label", render_mode);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "LocationUpdated");
  put("event_action", "Gps");
  put("event_label", latitude/longitude);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
      put("lat", latitude);
      put("lng", longitude);
  }});
}};
```

```java
new HashMap<String, Object>(){{
  put("type", "EVENT_LOGGED");
  put("event_category", "LocationUpdated");
  put("event_action", "Beacon");
  put("event_label", latitude/longitude);
  put("fields_object", new HashMap<String, Object>(){{
      put("map_id", map_id);
      put("lat", latitude);
      put("lng", longitude);
  }});
}};
```

___

#### <a name="LOCATION_SERVICE">Check Location Service</a>

dispatched after Tapped GPS Button in TMaps. You Should turn on Location Service and then dispatch <a href="#start_updating_location">Start</a> to begin updating location  

```java
new HashMap<String, Object>(){{
  put("type", "CHECK_GPS_AVAILABILITY");
}};
```

#### <a name="BEACON_LOCATION_SERVICE">Check Beacon Location Service</a>

dispatched after Tapped Beacon Location Button in TMaps. You Should turn on Bluetooth Service and then dispatch <a href="#start_updating_beacon_location">Start</a> to begin updating location  

```java
new HashMap<String, Object>(){{
  put("type", "CHECK_BEACON_LOCATION_AVAILABILITY");
}};
```

#### <a name="START_POSITION_UPDATES_FOR_BEACON_LOCATION">Start Beacon Location Manager</a>

dispatched after starting updating beacon location in TMaps. You Should start or stop beacon manager according to beacon manager state. if beacon manager state is true you shoud start beacon manager then dispatch the new position to TMaps to <a href="#SET_USER_BEACON_LOCATION">update</a> user location

```java
new HashMap<String, Object>(){{
  put("type", "START_POSITION_UPDATES_FOR_BEACON_LOCATION");
  put("start_beacon_manager",start_beacon_manager);
}};
```

&nbsp;&nbsp;&nbsp;&nbsp;**start_beacon_manager** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *boolean*  beacon manager state



### <a name="your_app_to_tmaps">Your APP actions dispatched to TMaps</a>

#### <a name="set_tenant_data">Set tenant data</a>

dispatch it to set tenants of map. 

```java
new HashMap<String, Object>(){{
  put("type", "SET_TENANT_DATA");
  put("payload", tenants_json);
}}
```

&nbsp;&nbsp;&nbsp;&nbsp;**payload** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *List* with *Map*, each *Map* with *id*, *feature_id*, *name*, *booth_id*, *icon*, *CUSTOM_KEYS_YOU_NEED* keys

___

#### <a name="SET_DEFAULT_FEATURE_POPUP_TEMPLATE">Set default feature popup template</a>

dispatch it to change default feature popup template.

```java
new HashMap<String, Object>(){{
  put("type", "SET_DEFAULT_FEATURE_POPUP_TEMPLATE");
  put("template", template);
  put("template_custom_data", templateCustomData);
}}
```

&nbsp;&nbsp;&nbsp;&nbsp;**template** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Required** valid *String* <a href="https://angular.io/guide/template-syntax">angular template</a> with <a href="#popup_scope">PopupScope</a> <br/>

&nbsp;&nbsp;&nbsp;&nbsp;<a name="template_custom_data">**template_custom_data**</a> <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Optional** *Map* with *CUSTOM_KEYS_YOU_NEED* keys and *String*, *Number*, *List*, *Map* values <br/>
template custom data of keys that you want to use from <a href="#popup_scope_custom_data">customData</a> in <a href="#popup_scope">PopupScope</a>

___

#### Set theme

dispatch it to change theme of map.

```java
new HashMap<String, Object>(){{
  put("type", "SET_THEME");
  put("theme", new HashMap<String, Object>(){{
     put("primary", primary_color);
     put("accent", accent_color);
  }});
}}
```

&nbsp;&nbsp;&nbsp;&nbsp;**primary** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Optional** valid *String* color

&nbsp;&nbsp;&nbsp;&nbsp;**accent** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Optional** valid *String* color

___

#### Load map

dispatch it to load map.

```java
new HashMap<String, Object>(){{
  put("type", "LOAD_MAP");
  put("map_id", map_id);
  put("theme", new HashMap<String, Object>(){{
     put("primary", primary_color);
     put("accent", accent_color);
  }});
  put("center", Arrays.asList(lng, lat));
  put("zoom", zoom);
}}
```

&nbsp;&nbsp;&nbsp;&nbsp;**map_id** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *String*

&nbsp;&nbsp;&nbsp;&nbsp;**theme** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Optional** *Map* with *primary*, *accent* keys and valid *String* color values
theme used for colors such as buttons and loading

&nbsp;&nbsp;&nbsp;&nbsp;**center** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Optional** *List* of *Numbers*
Default map center in longitude and latitude

&nbsp;&nbsp;&nbsp;&nbsp;**zoom** <br/>
&nbsp;&nbsp;&nbsp;&nbsp;**Optional** *Number*
Default zoom level

___

#### Change render mode

dispatch it to change render mode.

```java
new HashMap<String, Object>(){{
  put("type", "CHANGE_RENDER_MODE");
  put("modeToRender", modeToRender);
}}
```

&nbsp;&nbsp;&nbsp;&nbsp;**modeToRender**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *String* with *2D*, *3D*

___


#### Set zoom

dispatch it to change zoom of map.

```java
new HashMap<String, Object>(){{
  put("type", "SET_ZOOM");
  put("zoom", zoom);
  put("zoom_type", zoom_type);
}}
```

&nbsp;&nbsp;&nbsp;&nbsp;**zoom**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *Number*

&nbsp;&nbsp;&nbsp;&nbsp;**zoom_type**
&nbsp;&nbsp;&nbsp;&nbsp;**Optional** *String* with *FLY_TO*

___

#### Set center

dispatch it to change center of map.

```java
new HashMap<String, Object>(){{
  put("type", "SET_CENTER");
  put("center", Arrays.asList(lng, lat));
}}
```

&nbsp;&nbsp;&nbsp;&nbsp;**center**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *List* of *Numbers*
Default map center in longitude and latitude

___

#### <a name="HIGHLIGHT_FEATURE">Highlight feature</a>

dispatch it to highlight feature.

```java
new HashMap<String, Object>(){{
  put("type", "HIGHLIGHT_FEATURE");
  put("feature_id", feature_id);
}}
```

&nbsp;&nbsp;&nbsp;&nbsp;**feature_id**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *String* with valid feature id

___

#### Mark feature

dispatch it to mark feature.

```java
new HashMap<String, Object>(){{
  put("type", "MARK_FEATURE");
  put("feature_id", feature_id);
}}
```

&nbsp;&nbsp;&nbsp;&nbsp;**feature_id**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *String* with valid feature id

___

#### Show GPS Button

dispatch it after Map Loaded to show GPS button

```java
new HashMap<String, Object>(){{
  put("type", "ENABLE_GPS_BUTTON");
}}
```

**Don't forget to add permissions** to AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.ACCESS_GPS" />
<uses-permission android:name="android.permission.ACCESS_ASSISTED_GPS" />
<uses-permission android:name="android.permission.ACCESS_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

** Follow our samples **
 <br/>
https://github.com/tagipedia/tmaps-android-sample/blob/1f109c83e37a6e569e9bef7b71f2ca6190d44636/app/src/main/java/com/tagipedia/tmaps/MainActivity.java#L107-L115
 <br/>
https://github.com/tagipedia/tmaps-android-sample/blob/1f109c83e37a6e569e9bef7b71f2ca6190d44636/app/src/main/java/com/tagipedia/tmaps/MainActivity.java#L134-L200
 <br/>
https://github.com/tagipedia/tmaps-android-sample/blob/1f109c83e37a6e569e9bef7b71f2ca6190d44636/app/src/main/java/com/tagipedia/tmaps/MainActivity.java#L259-L264
 <br/>
https://github.com/tagipedia/tmaps-android-sample/blob/1f109c83e37a6e569e9bef7b71f2ca6190d44636/app/src/main/java/com/tagipedia/tmaps/MainActivity.java#L277-L284
 <br/>
https://github.com/tagipedia/tmaps-android-sample/blob/1f109c83e37a6e569e9bef7b71f2ca6190d44636/app/src/main/java/com/tagipedia/tmaps/MainActivity.java#L304-L344
 <br/>
___

#### <a name="start_updating_location">Start Updating Location using GPS</a>

dispatch it after <a href="#LOCATION_SERVICE">check</a> location service to start updating user location and showing nearest places to user

```java
new HashMap<String, Object>(){{
  put("type", "START_UPDATING_LOCATION");
  put("is_gps_activated", is_gps_activated);
}}
```
&nbsp;&nbsp;&nbsp;&nbsp;**is_gps_activated**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *boolean* 

___
#### Show Beacon Location Button

dispatch it after Map Loaded to show Beacon Location button

```java
new HashMap<String, Object>(){{
  put("type", "ENABLE_BEACON_LOCATION_BUTTON");
}}
```

___
#### <a name="start_updating_beacon_location">Start Updating Location using Beacon Location</a>

dispatch it after <a href="#BEACON_LOCATION_SERVICE">check</a> beacon location service to start updating user location and showing nearest places to user

```java
new HashMap<String, Object>(){{
  put("type", "START_UPDATING_BEACON_LOCATION");
  put("is_beacon_location_activated", is_beacon_location_activated);
}}
```

&nbsp;&nbsp;&nbsp;&nbsp;**is_beacon_location_activated**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *boolean* 

___
#### <a name="SET_USER_BEACON_LOCATION">Update User Location (Beacon Location)</a>

dispatch it after beacon location service <a href="#START_POSITION_UPDATES_FOR_BEACON_LOCATION">started</a> to update user location and showing nearest places to user

```java
new HashMap<String, Object>(){{
  put("type", "SET_USER_BEACON_LOCATION");
  put("x", x);
  put("y", y);
  put("origin_lat", origin_lat);
  put("origin_lng", origin_lng);
}}
```

&nbsp;&nbsp;&nbsp;&nbsp;**x**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *Number* </br>
&nbsp;&nbsp;&nbsp;&nbsp;**y**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *Number* </br>
&nbsp;&nbsp;&nbsp;&nbsp;**origin_lat**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *Number* </br>
&nbsp;&nbsp;&nbsp;&nbsp;**origin_lng**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *Number* 


___
#### <a name="SET_DEVICE_DATA">Set device data</a>

dispatch it to set device id and device type 

```java
new HashMap<String, Object>(){{
  put("type", "SET_DEVICE_DATA");
  put("device_id", device_id);
  put("device_type", "ANDROID");
}}
```
&nbsp;&nbsp;&nbsp;&nbsp;**device_id**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *String* 

&nbsp;&nbsp;&nbsp;&nbsp;**device_type**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *String* with ANDROID

___
#### <a name="SET_APPLICATION_SECRETS">Set application secrets</a>

dispatch it to set client id and client secret

```java
new HashMap<String, Object>(){{
  put("type", "SET_APPLICATION_SECRETS");
  put("client_id", client_id);
  put("client_secret", client_secret);
}}
```
&nbsp;&nbsp;&nbsp;&nbsp;**client_id**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *String* 

&nbsp;&nbsp;&nbsp;&nbsp;**client_secret**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *String* 

## Types

### <a name="popup_scope">PopupScope</a>
#### poi

current feature

```js
poi
```

&nbsp;&nbsp;&nbsp;&nbsp;**poi**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *<a href="#poi">poi</a>*

___
#### enableRouting

*Boolean* to check if routing is enabled

```js
enableRouting
```
___
#### showRoutingDialog

method to show routing dialog.

```js
showRoutingDialog($event, data)
```

&nbsp;&nbsp;&nbsp;&nbsp;**$event**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *<a href="https://angular.io/guide/template-syntax#event-binding---event-">$event</a>*

&nbsp;&nbsp;&nbsp;&nbsp;**data**
&nbsp;&nbsp;&nbsp;&nbsp;**Optional** *Object* with *from*, *to* keys and *<a href="#poi">poi</a>* value

___
#### applyIfneeded

method to call <a href="https://docs.angularjs.org/api/ng/type/$rootScope.Scope#$apply">Angular $apply</a>

```js
applyIfneeded(callback)
```

&nbsp;&nbsp;&nbsp;&nbsp;**callback**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *Function*

___
#### <a name="popup_scope_custom_data">customData</a>

custom data object that have all keys of <a href="#template_custom_data">template_custom_data</a>

```js
customData[key]
```

&nbsp;&nbsp;&nbsp;&nbsp;**key**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** with valid key from <a href="#template_custom_data">template_custom_data</a>
___
#### dispatch

method to dispatch action from your APP to TMaps.

```js
dispatch(action)
```

&nbsp;&nbsp;&nbsp;&nbsp;**action**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *Object* with valid <a href="#your_app_to_tmaps">action</a>
___
#### <a name="dispatchToContainer">dispatchToContainer</a>

method to dispatch action from TMaps to your APP.

```js
dispatchToContainer(action)
```

&nbsp;&nbsp;&nbsp;&nbsp;**action**
&nbsp;&nbsp;&nbsp;&nbsp;**Required** *Object* with *type*, *CUSTOM_KEYS_YOU_NEED* keys.<br />
*type* is *String* value.<br />
*CUSTOM_KEYS_YOU_NEED* is any of *String*, *Number*, *List*, *Map* values.<br />
___
#### closeInfo

method to close feature popup

```js
closeInfo()
```

### <a name="poi">poi</a>

#### id
id of feature

```js
poi.id
```
___
#### category
category of feature

```js
poi.category
```
___
#### tags

tags of feature

```js
poi.tags
```
___
#### getTenant

tenant of feature you set in <a href="#set_tenant_data">SET_TENANT_DATA</a>

```js
poi.getTenant()
```
___
#### <a name="getTenantName">getTenantName</a>

tenant name of feature you set in <a href="#set_tenant_data">SET_TENANT_DATA</a>

```js
poi.getTenantName()
```
___
#### <a name="getTenantIcon">getTenantIcon</a>

tenant icon of feature you set in <a href="#set_tenant_data">SET_TENANT_DATA</a>

```js
poi.getTenantIcon()
```
___
#### <a name="getTenantBoothId">getTenantBoothId</a>

tenant booth_id of feature you set in <a href="#set_tenant_data">SET_TENANT_DATA</a>

```js
poi.getTenantBoothId()
```
___
#### getDisplayName

<a href="#getTenantName">getTenantName</a> or feature name

```js
poi.getDisplayName()
```
___
#### hasName

check if tenant or feature have name. 

```js
poi.hasName()
```
___
#### getDisplayCategory

get first category of tenant or feature if there is category.

```js
poi.getDisplayCategory()
```
___
#### hasCategories

check if tenant or feature have categories. 

```js
poi.hasCategories()
```
___
#### isBuilding

check if feature is building.

```js
poi.isBuilding()
```
___
#### getIcon

<a href="#getTenantIcon">getTenantIcon</a> or feature icon

```js
poi.getIcon()
```
___
#### getBoothId

<a href="#getTenantBoothId">getTenantBoothId</a> or feature booth id

```js
poi.getBoothId()
```

## Advanced Scenrios (Look at sample for implementation of scenrios)

### Highlight initial feature

#### if you dispatch SET_ZOOM/SET_CENTER

you should dispatch <a href="#HIGHLIGHT_FEATURE">HIGHLIGHT_FEATURE</a> for initial feature after <a href="#ZOOM_ENDED">ZOOM_ENDED</a>/<a href="#CENTER_ENDED">CENTER_ENDED</a>

#### if you don't dispatch SET_ZOOM/SET_CENTER

you should dispatch <a href="#HIGHLIGHT_FEATURE">HIGHLIGHT_FEATURE</a> for initial feature after <a href="#MAP_LOADED">MAP_LOADED</a>

### Add custom buttons inside feature popup template

you should dispatch <a href="#SET_DEFAULT_FEATURE_POPUP_TEMPLATE">SET_DEFAULT_FEATURE_POPUP_TEMPLATE</a> for your customized template and add your custom buttons and use <a href="#dispatchToContainer">dispatchToContainer</a> when button clicked to handle the click action in your APP.


## Important Notes 
### Our default feature popup template

``` html
<md-card id="{{poi.id}}" class="feature-popup display-none slide-up">
  <div layout="row" layout-align="end start" >
    <md-card-title class="padding-bottom-overide" layout="row" layout-align="center center">
      <md-card-title-media ng-if="poi.getDisplayName()">
        <div class="circle" layout="row" layout-align="center center">
          <md-icon ng-if="!poi.getIcon()" md-font-set="material-icons" class="camera-icon">camera_alt</md-icon>
          <img ng-if="poi.getIcon()" ng-src="{{poi.getIcon()}}" />
        </div>
      </md-card-title-media>
      <md-card-title-text >
        <div class="md-subline">{{poi.getDisplayName() || poi.getDisplayCategory()}}</div>
        <div ng-if="poi.getBoothId()" layout="row">
          <div class="categoryline" >
            <div class="margin-right-margin-left">{{poi.getBoothId()}}</div>
          </div>
        </div>
        <div ng-if="poi.category" layout="row">
          <div class="categoryline" >
            <div class="margin-right-margin-left">{{poi.category}}</div>
          </div>
        </div>
      </md-card-title-text>
    </md-card-title>
    <md-button id="close-info" class="md-icon-button" ng-click="closeInfo()">
      <md-icon md-font-set="material-icons" aria-label="close info">close</md-icon>
    </md-button>
  </div>
  <md-card-actions layout="row" layout-align="center center" >
    <div ng-if="poi.category === 'wheelchair-bathroom'" layout="row" layout-align="center center">
      <i class="fa fa-3x fa-wheelchair"></i>
    </div>
    <!-- <md-divider></md-divider> -->

    <br ng-if="poi.category === 'wheelchair-bathroom'"  />
    <br ng-if="poi.category === 'wheelchair-bathroom'"  />
    <div flex ng-if="enableRouting && poi.entrances && poi.entrances.length" class="feature-routing-buttons" layout="row" layout-align="center center">
      <md-button layout="row" class="md-raised md-primary" ng-click="showRoutingDialog($event, {from: poi})">
        <div class="margin-right-margin-left"> Route From</div>
        <md-icon md-font-set="material-icons" >adjust</md-icon>
      </md-button>
      <md-button layout="row" class="md-raised md-primary" ng-click="showRoutingDialog($event, {to:poi})">
        <div class="margin-right-margin-left"> Route To</div>
        <md-icon md-font-set="material-icons" >flag</md-icon>
      </md-button>
    </div>
  </md-card-actions>
</md-card>
```





