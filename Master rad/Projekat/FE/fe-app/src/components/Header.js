import React from 'react'
import Button from './Button'


const Header = (props) => {

    return (
        <div>
            <header>
            <header className='header'>
                <h2>Light Intensity </h2>
                <h2>{props.lightIntensity}</h2>
            </header>
            <header className='header'>
                <h2>Ground Moisture</h2>
                <h2>{props.groundMoisture}</h2>
            </header>
            <header className='header'>
                <h2>Wind Intensity</h2>
                <h2>{props.windIntensity}</h2>
            </header>
            <header className='header'>
                <h2>Soil Humidity</h2>
                <h2>{props.soilHumidity}</h2>
            </header>
            </header>
        </div>
    )
}

export default Header
