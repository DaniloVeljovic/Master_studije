import Header from "./components/Header";
import React, { useState } from "react";
import Button from "./components/Button";

function App() {

  const address = "http://192.168.1.9";

  const [temp,setTemp] = useState(0);
  const [light,setLight] = useState(0);
  const [gm,setGM] = useState(0);
  const [lenght, setLenght] = useState('')


  const onClick = () => {
    console.log('click click')
    const fetchNewMeasurements = async() => {
        let res = await fetch(address + ':8080/api/sensors/1129 AND  value > 80')
        let data = await res.json()

        console.log(data)

        setTemp(data.value);

         res = await fetch(address + ':8080/api/sensors/1130')
         data = await res.json()

        console.log(data)

        setLight(data.value);

         res = await fetch(address + ':8080/api/sensors/1131')
         data = await res.json()

        console.log(data)

        setGM(data.value);
        
    }
    fetchNewMeasurements()
}

const irrigate = async () => {
  const rawResponse = await fetch(address + ':8080/api/irrigation', {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({"length" : lenght})
  });
  const content = await rawResponse.json();

  console.log(content);
}

  return (
    <div>
      <div className="container">
        <h1 style ={{textAlign: 'center'}}>Sensor Measurements</h1>
       <Header dataTemp={temp} dataLight={light} dataGM={gm} onClick={onClick} />
      </div>
      <div style ={{textAlign: 'center'}} className="container">
        <h1 style ={{textAlign: 'center'}}>Irrigate</h1>
        <h4 style ={{textAlign: 'center'}}>Enter the number of ms</h4>
        <input onChange={event => setLenght(event.target.value)} ></input>
        <Button color='green' text='IRRIGATE' onClick={irrigate} />
      </div>
    </div>
    
  );
}

export default App;
