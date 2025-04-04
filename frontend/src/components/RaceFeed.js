import React, { useEffect, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";  // Import the Client from @stomp/stompjs

const RaceFeed = () => {
  const [raceUpdates, setRaceUpdates] = useState([]);
  const [pitUpdates, setPitUpdates] = useState([]);

  useEffect(() => {
    // Connect to the WebSocket server
    const socket = new SockJS("http://localhost:8080/ws");
    const stompClient = new Client({
      brokerURL: "ws://localhost:8080/ws",
      connectHeaders: {},
      debug: (str) => {
        console.log(str);
      },
      onConnect: (frame) => {
        console.log("Connected: " + frame);

        // Subscribe to the lap updates
        stompClient.subscribe("/topic/lap", (message) => {
          const lap = JSON.parse(message.body);
          const lapUpdate = `🏁 ${lap.rider.name} completed lap ${lap.lapNumber} in ${lap.lapTimeMillis}ms`;
          setRaceUpdates((prevUpdates) => [...prevUpdates, lapUpdate]);
        });

        // Subscribe to the pit updates
        stompClient.subscribe("/topic/pit", (message) => {
          const pit = JSON.parse(message.body);
          const pitUpdate = `🛠️ ${pit.rider.name} is in pit for ${pit.type} (wait: ${pit.waitTimeMillis}ms)`;
          setPitUpdates((prevUpdates) => [...prevUpdates, pitUpdate]);
        });
      },
    });

    stompClient.activate();

    // Cleanup the WebSocket connection on component unmount
    return () => {
      stompClient.deactivate();
    };
  }, []);

  return (
    <div>
      <h2>🏍️ MotoGP Live Race Feed</h2>
      <h3>Race Updates</h3>
      <div id="raceFeed">
        {raceUpdates.map((update, index) => (
          <p key={index}>{update}</p>
        ))}
      </div>
      <h3>Pit Stop Updates</h3>
      <div id="pitFeed">
        {pitUpdates.map((update, index) => (
          <p key={index}>{update}</p>
        ))}
      </div>
    </div>
  );
};

export default RaceFeed;
