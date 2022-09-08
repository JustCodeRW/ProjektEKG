#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>

BLECharacteristic *pCharacteristic;
bool deviceConnected = false;
double txValue = 0;

#define SERVICE_UUID        "92fae7ae-1d8e-11ed-861d-0242ac120002"
#define CHARACTERISTIC_UUID "92fae9fc-1d8e-11ed-861d-0242ac120002"

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
    }
};

void setup() {
  Serial.begin(115200);
  Serial.println("Starting BLE work!");

  BLEDevice::init("ESP32");
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  BLEService *pService = pServer->createService(SERVICE_UUID);
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_WRITE |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );

  pCharacteristic->addDescriptor(new BLE2902());
  pCharacteristic->setValue("Hello World says Neil");
  pService->start();
  // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");
}

void loop() {
  // put your main code here, to run repeatedly:
  double testValues[] = {0.811678, 0.689376, 0.137381, 0.042543, 0.011492, 0.055901, 0.025971, 0.199248, 0.255929, 0.323547, 0.301444, 0.314244, 0.292905, 0.317687};
  if (deviceConnected) {
    //Conversion of txValue
    char values[8];

    for (int i = 0; i < 14; i++) {
      dtostrf(testValues[i], 7, 6, values);
      //Setting the value to the characteristic
      pCharacteristic->setValue(values);
      //Notifing the connected client
      pCharacteristic->notify();
      Serial.println("Sent value: " + String(values));
      delay(500);
    }
  }
}
