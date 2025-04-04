import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

function WebSocketClient() {
  const [laps, setLaps] = useState([]);
  const [pitStops, setPitStops] = useState([]);

  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws'); // same as your endpoint
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
      console.log('Connected: ' + frame);

      stompClient.subscribe('/topic/lap', function(message) {
        const lap = JSON.parse(message.body);
        const display = `🏁 ${lap.rider.name} completed lap ${lap.lapNumber} in ${lap.lapTimeMillis}ms`;
        setLaps(prevLaps => [...prevLaps, display]);
      });

      stompClient.subscribe('/topic/pit', function(message) {
        const pit = JSON.parse(message.body);
        const display = `🛠️ ${pit.rider.name} is in pit for ${pit.type} (wait: ${pit.waitTimeMillis}ms)`;
        setPitStops(prevPitStops => [...prevPitStops, display]);
      });
    });

    return () => {
      stompClient.disconnect();
    };
  }, []);

  return (
    <div>
      <h2>🏍️ MotoGP Live Race Feed</h2>
      <h3>Laps</h3>
      <div id="lapFeed">
        {laps.map((lap, index) => (
          <p key={index}>{lap}</p>
        ))}
      </div>

      <h3>Pit Stops</h3>
      <div id="pitFeed">
        {pitStops.map((pit, index) => (
          <p key={index}>{pit}</p>
        ))}
      </div>
    </div>
  );
}

export default WebSocketClient;
