const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendReservationPush = onDocumentCreated(
  "reservations/{userId}/userReservations/{reservationId}",
  async (event) => {
    try {
      const reservation = event.data?.data();
      if (!reservation) return;

      const userId = event.params.userId;
      const reservationId = event.params.reservationId;
      const name = reservation.name || "Customer";

      const title = "Reservation Successful";
      const body = `Hi ${name}, your reservation is booked and waiting for approval.`;

      // 🔔 PUSH
      const userDoc = await admin.firestore().collection("users").doc(userId).get();
      if (userDoc.exists && userDoc.data().fcmToken) {
        await admin.messaging().send({
          notification: { title, body },
          token: userDoc.data().fcmToken
        });
      }

      // 🗂️ SAVE TO FIRESTORE
      await admin.firestore()
        .collection("notifications")
        .doc(userId)
        .collection("userNotifications")
        .add({
          title,
          message: body,
          type: "created",
          reservationId,
          read: false,
          timestamp: admin.firestore.FieldValue.serverTimestamp()
        });

      console.log("Reservation created notification saved");

    } catch (e) {
      console.error("Create notification error:", e);
    }
  }
);

exports.sendCancelReservationPush = onDocumentUpdated(
  "reservations/{userId}/userReservations/{reservationId}",
  async (event) => {
    try {
      const before = event.data.before.data();
      const after = event.data.after.data();

      if (!before || !after) return;
      if (before.status === after.status) return;
      if (after.status !== "cancelled") return;

      const userId = event.params.userId;
      const reservationId = event.params.reservationId;
      const name = after.name || "Customer";

      const title = "Reservation Cancelled";
      const body = `Hi ${name}, your reservation has been cancelled successfully.`;

      // 🔔 PUSH
      const userDoc = await admin.firestore().collection("users").doc(userId).get();
      if (userDoc.exists && userDoc.data().fcmToken) {
        await admin.messaging().send({
          notification: { title, body },
          token: userDoc.data().fcmToken
        });
      }

      // 🗂️ SAVE TO FIRESTORE
      await admin.firestore()
        .collection("notifications")
        .doc(userId)
        .collection("userNotifications")
        .add({
          title,
          message: body,
          type: "cancelled",
          reservationId,
          read: false,
          timestamp: admin.firestore.FieldValue.serverTimestamp()
        });

      console.log("Reservation cancelled notification saved");

    } catch (e) {
      console.error("Cancel notification error:", e);
    }
  }
);
