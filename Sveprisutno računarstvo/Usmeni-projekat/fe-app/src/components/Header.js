import React from 'react'
import Button from './Button'


const Header = (props) => {

    return (
        <div>
            <header>
            <header className='header'>
                <h2>Temperature </h2>
                <h2>{props.dataTemp}</h2>
            </header>
            <header className='header'>
                <h2>Light</h2>
                <h2>{props.dataLight}</h2>
            </header>
            <header className='header'>
                <h2>Ground moisture</h2>
                <h2>{props.dataGM}</h2>
            </header>
            <Button color='green' text='REFRESH' onClick={props.onClick} />
            </header>
            
        </div>
    )
}

export default Header
