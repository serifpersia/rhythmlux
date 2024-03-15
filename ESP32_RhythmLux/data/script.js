var Socket;

// Function to initialize WebSocket
function init() {
  Socket = new WebSocket("ws://" + window.location.hostname + ":81/");

  // Add error event handler
  Socket.addEventListener("error", function(error) {
    console.error("WebSocket error:", error);
    // Handle the error here, e.g., display an error message to the user.
  });

  // Add close event handler
  Socket.addEventListener("close", function(event) {
    if (event.wasClean) {
      console.log(
        "WebSocket closed cleanly, code=" +
          event.code +
          ", reason=" +
          event.reason
      );
    } else {
      console.error("WebSocket connection died");
      // You may want to attempt to reconnect here.
    }
  });

  // Add open event handler
  Socket.addEventListener("open", function(event) {
    console.log("WebSocket connection opened");
    sendData("RequestValues");
  });

  // Event listener to handle updates from the server
  Socket.addEventListener("message", function(event) {
    var data = JSON.parse(event.data);
    console.log("Received data from the server:", data);

    // Call the updateUI function to update the UI elements
    //updateUI(data);
  });
}

// Function to send data via WebSocket with error handling
function sendData(action, data) {
  if (Socket.readyState === WebSocket.OPEN) {
    try {
      Socket.send(JSON.stringify({ action: action, ...data }));
    } catch (error) {
      console.error("Error sending data:", error);
      // Handle the error here, e.g., display an error message to the user.
    }
  } else {
    console.error("WebSocket connection not open");
    // Handle the error here, e.g., display an error message to the user or attempt to reconnect.
  }
}

document.getElementById("ledCurrent").value = 450;

document
  .getElementById("configureButton")
  .addEventListener("click", function() {
    var ssid = document.getElementById("ssid").value;
    var password = document.getElementById("password").value;
    var ledCurrent = document.getElementById("ledCurrent").value;
    
    // Sending data to the server
    sendData("configureNetworkAction", {
      wifi: ssid,
      password: password,
      ledCurrent: ledCurrent
    });
  });

// Call the init function when the window loads
window.onload = function(event) {
  init();
};
