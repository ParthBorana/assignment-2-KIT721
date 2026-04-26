# Interior Design Quoting App (KIT721 Assignment 2)

## Device for Testing

The application was tested on a physical Android device:  
Samsung Galaxy M34 5G (Model: SM-M346B/DS)

The Android emulator could not be used due to a hypervisor/virtualization error on my laptop.  
All features were tested successfully on this device.

---

## Activities Overview

### MainActivity
Displays the list of houses. Users can add a new house or open an existing one.

### AddEditHouseActivity
Allows the user to create a new house or edit/delete an existing house. Stores customer name and address.

### HouseDetailActivity
Displays all rooms inside a selected house. Allows adding new rooms.

### AddEditRoomActivity
Allows the user to create, edit, or delete a room. Includes a camera feature to capture and store a room image.

### RoomDetailActivity
Displays windows and floor spaces inside a selected room. Allows adding windows and floor spaces.

### AddEditWindowActivity
Allows the user to create or edit window details. Includes:
- Width and height input
- Product selection using API
- Product validation based on constraints
- Saving selected product details

### AddEditFloorActivity
Allows the user to create or edit floor space details. Includes:
- Width and depth input
- Product selection using API
- Saving selected floor product details

### QuoteActivity
Displays the full quote summary including:
- Window and floor prices
- Labour cost per room
- Total cost calculation
- Item selection/deselection
- Custom package pricing feature
- Share quote functionality

---

## Features

- Create, edit, and delete houses, rooms, windows, and floor spaces
- Firebase Firestore data storage using nested collections
- Product selection using external API
- Window constraint validation (width, height, multi-panel, rigid rules)
- Floor product selection without constraints
- Dynamic quote calculation with area conversion
- Labour cost calculation per room
- Item selection/deselection in quote
- Camera integration for room images
- Share quote feature using Android Intent
- Custom feature: premade design packages with discounted pricing

---

## Notes

- API is used to load product data dynamically for both window and floor selection.
- No third-party libraries were used.
- The application follows concepts taught in lectures and tutorials, with additional logic implemented where required.
