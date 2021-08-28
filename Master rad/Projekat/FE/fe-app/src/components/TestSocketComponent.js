const [message, setMessage] = useState('You server message here.');

let onConnected = () => {
  console.log("Connected!!")
}

let onMessageReceived = (msg) => {
  console.log(msg)
  setMessage(JSON.stringify(msg));
}

return (
  <div>
    <SockJsClient
      url={SOCKET_URL}
      topics={['/topic/message']}
      onConnect={onConnected}
      onDisconnect={console.log("Disconnected!")}
      onMessage={msg => onMessageReceived(msg)}
      debug={false}
    />
    <div>{message}</div>
  </div>
);