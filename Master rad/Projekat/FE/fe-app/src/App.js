import Header from "./components/Header";
import React, { useState } from "react";
import SockJsClient from 'react-stomp';
import Button from "./components/Button";
import SimpleList from "./components/SimpleList";
import DateAndTimePickers from "./components/DateAndTimePickers";

const SOCKET_URL = 'http://localhost:8082/ws-message';
const SOCKET_URL_DEVICE = 'http://localhost:8084/ws-message';
const address = "http://localhost:8082";
let queue = []

function App() {
  const [lightIntensity,setLightIntensity] = useState(0);
  const [windIntensity,setWindIntensity] = useState(0);
  const [groundMoisture,setGroundMoisture] = useState(0);
  const [soilHumidity, setSoilHumidity] = useState(0)
  const [testList, setTestList] = useState(["test", "test2"])
  const [dateFrom, setDateFrom] = useState("2021-08-19T13:50:35.098Z")
  const [dateTo, setDateTo] = useState("2021-08-19T13:50:35.098Z")
  const [lightIntensityThreshold, setLightIntensityThreshold] = useState(0)
  const [lightIntensityThresholdList, setLightIntensityThresholdList] = useState([])
  const [deviceActuatedQueue, setDeviceActuatedQueue] = useState([])
  

let onConnected = () => {
  console.log("Connected!!")
}

let onMessageReceived = (msg) => {
  setGroundMoisture(msg.groundMoisture)
  setLightIntensity(msg.lightIntensity)
  setWindIntensity(msg.windIntensity)
  setSoilHumidity(msg.soilHumidity)
}

let onMessageReceivedFromDevice = (msg) => {
      let stringMsg = "Device id: " + msg.deviceId + ", status: " + msg.status + ", pinToActivate: " + msg.pinToActivate + ", activationDate: " + msg.activationDate + ", length: " + msg.length;
      queue.unshift(stringMsg)
      if(queue.length > 5)
        queue.pop()
      setDeviceActuatedQueue(queue)
}

let handleChangeInputForLightIntesity = (event) => {
  setLightIntensityThreshold(event.target.value)
}

let handleChangeDateTo = (event) => {
  const date = event.target.value + ':00.000Z'
  setDateTo(date)
}

let handleChangeDateFrom = (event) => {
  const date = event.target.value.toString() + ':00.000Z'
  console.log(date)
  setDateFrom(date)
}

const handleGetBetweenTwoDates = async () => {
  let res = await fetch(address + "/api/sensors/from/" + dateFrom + "/to/" + dateTo)
  const content = await res.json();
  let result = []
  content.forEach(element => {
    result.push("Light Intensity:"+element.lightIntensity+" Ground Moisture: " + element.groundMoisture +" Soil Humidity: "+ element.soilHumidity + " Wind Intensity: " + element.windIntensity)
  });
  setTestList(result)
}

const handleGetLightIntensityOverAThreshold = async () => {
  let res = await fetch(address + "/api/sensors/lightIntensity/" + lightIntensityThreshold)
  const content = await res.json();
  let result = []
  content.forEach(element => {
    result.push("Light Intensity:"+element.lightIntensity+" Ground Moisture: " + element.groundMoisture +" Soil Humidity: "+ element.soilHumidity + " Wind Intensity: " + element.windIntensity)
  });
  setLightIntensityThresholdList(result)
}

  return (
    <div>
      <div className="container">
        <h1 style ={{textAlign: 'center'}}>Latest Sensor Measurements</h1>
        <Header lightIntensity={lightIntensity} windIntensity={windIntensity} groundMoisture={groundMoisture} soilHumidity={soilHumidity} />
      </div>
      <div className="container">
        <h1 style ={{textAlign: 'center'}}>Get sensor measurements between two dates</h1>
        <div className="small_container">
        <div >
          <div>Enter date from</div>
          <DateAndTimePickers onChange={handleChangeDateFrom}/>
        </div>
        <div >
          <div>Enter date to</div>
          <DateAndTimePickers onChange={handleChangeDateTo}/>
        </div>
        <Button color='green' text='REFRESH' onClick={handleGetBetweenTwoDates}/>
        </div>
        <SimpleList prop={testList}/>
      </div>
      
      <div className="container">
        <h1 style ={{textAlign: 'center'}}>Get Light Intensity over a threshold </h1>
        <div className="small_container">
          <div>Enter light intensity threshold:</div>
          <input onChange={handleChangeInputForLightIntesity}/>
          <Button color='green' text='REFRESH' onClick={handleGetLightIntensityOverAThreshold}/>
        </div>
        <SimpleList prop={lightIntensityThresholdList}/>
      </div>
      <div className="container">
        <h1 style ={{textAlign: 'center'}}>Last actuated devices</h1>
        <SimpleList prop={deviceActuatedQueue}/>
      </div>
      
      <div>

    <SockJsClient
      url={SOCKET_URL}
      topics={['/topic/message']}
      onConnect={onConnected}
      onDisconnect={console.log("Disconnected!")}
      onMessage={msg => onMessageReceived(msg)}
      debug={false}
    />

<SockJsClient
      url={SOCKET_URL_DEVICE}
      topics={['/topic/message']}
      onConnect={console.log("Device connected!")}
      onDisconnect={console.log("Device disconnected!")}
      onMessage={msg => onMessageReceivedFromDevice(msg)}
      debug={false}
    />
  </div>
</div>
  );
}

export default App;
