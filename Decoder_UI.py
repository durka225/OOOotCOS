import sys
import Decoder_API
from PyQt5.QtWidgets import QApplication, QWidget, QPushButton, QVBoxLayout, QLineEdit, QLabel, QGridLayout
from PyQt5.QtGui import QFont, QPixmap
from PyQt5.QtGui import QPalette, QColor
from PyQt5.QtCore import Qt

class SimpleWindow(QWidget):
    def __init__(self):
        super().__init__()
        self.api_connected = False
        self.init_ui()

    def init_ui(self):
        self.decoded_message = ""
        self.decoded_symbol = ""
        self.fast_mode = False

        # --- Кнопки ---
        self.toggle_connection = QPushButton("Connect Arduino")
        self.toggle_connection.setCheckable(True)
        self.toggle_connection.setFixedSize(360, 100)
        self.toggle_connection.clicked.connect(self.toggle_connection_func)

        self.toggle_clear = QPushButton("Clear Message")
        self.toggle_clear.setCheckable(True)
        self.toggle_clear.setFixedSize(360, 100)
        self.toggle_clear.clicked.connect(self.toggle_clear_func)

        self.toggle_fast_mode = QPushButton("Fast Mode: OFF")
        self.toggle_fast_mode.setCheckable(True)
        self.toggle_fast_mode.setFixedSize(360, 100)
        self.toggle_fast_mode.clicked.connect(self.toggle_fast_mode_func)

        # --- Поля статуса ---
        self.text = QLineEdit("Arduino Disconnected")
        self.text.setFixedSize(1200, 80)
        self.text.setReadOnly(True)

        self.text1 = QLineEdit()
        self.text1.setFixedSize(1200, 240)
        self.text1.setReadOnly(True)
        self.text1.setPlaceholderText("Decoded message will appear here...")

        self.com_choice = QLineEdit()
        self.com_choice.setPlaceholderText("Enter the COM port")
        self.com_choice.setFixedSize(360, 100)

        self.logo_label = QLabel(self)
        pixmap = QPixmap("./imageUI1.png")
        self.logo_label.setPixmap(pixmap.scaledToWidth(350, Qt.SmoothTransformation))
        self.logo_label.setAlignment(Qt.AlignCenter)
        self.logo_label.setObjectName("logo_label")
        self.logo_label.setContentsMargins(0, 50, 200, 0)

        layout = QGridLayout()
        
        button_layout = QVBoxLayout()
        button_layout.addWidget(self.toggle_connection, alignment=Qt.AlignCenter)
        button_layout.addWidget(self.toggle_clear, alignment=Qt.AlignCenter)
        button_layout.addWidget(self.com_choice, alignment=Qt.AlignCenter)
        button_layout.setContentsMargins(0, 0, 0, 0)

        button_container = QWidget()
        button_container.setLayout(button_layout)
        layout.addWidget(button_container, 0, 0, 2, 1, alignment=Qt.AlignCenter)
        button_container.setContentsMargins(0, 0, 0, 0)
        button_layout.setSpacing(15)

        layout.addWidget(self.logo_label, 0, 1, 2, 1, alignment=Qt.AlignCenter)
        layout.addWidget(self.text, 2, 0, 1, 2, alignment=Qt.AlignCenter)
        layout.addWidget(self.text1, 3, 0, 1, 2, alignment=Qt.AlignCenter)
        layout.addWidget(self.text, 2, 0, 1, 2, alignment=Qt.AlignCenter)
        layout.addWidget(self.text1, 3, 0, 1, 2, alignment=Qt.AlignCenter)

        self.setLayout(layout)
        self.setWindowTitle('🎯 LED RGB MORSE DECODER')
        self.setGeometry(100, 100, 1400, 900)

    def toggle_connection_func(self, checked):
        if checked:
            self.toggle_connection.setText("Disconnect Arduino")
            self.connect_arduino()
        else:
            self.toggle_connection.setText("Connect Arduino")
            self.disconnect_arduino()
        if (self.fast_mode):
            Decoder_API.write("1")
        else:
            Decoder_API.write("0")

    def toggle_clear_func(self):
        self.decoded_message = ""
        self.text1.setText("")
        self.text1.setPlaceholderText("Decoded message will appear here...")

    def toggle_fast_mode_func(self, checked):
        if checked:
            self.fast_mode = True
            self.toggle_fast_mode.setText("Fast Mode")
            self.text.setText("🚀 Fast Mode Enabled - Quick Response")
        else:
            self.fast_mode = False
            self.toggle_fast_mode.setText("Slow Mode")
            self.text.setText("🐢 Normal Mode - Standard Response")


    def connect_arduino(self):
        if not self.api_connected:
            port = self.com_choice.text().strip()
            msg = Decoder_API.connect(port, 115200)
            Decoder_API.set_callback(self.update_parameters)
            Decoder_API.start()
            print("yo")
            self.com_choice.setFixedSize(560, 100)
            self.com_choice.setText(msg[1])
            self.api_connected = True
            self.text.setText("✅ Decoder Connected - Ready for Decoding")
        else:
            self.text.setText("ℹ️ Decoder is already connected")

    def disconnect_arduino(self):
        if self.api_connected:
            Decoder_API.close()
            self.api_connected = False
            self.text.setText("🔌 Arduino Disconnected")
        else:
            self.text.setText("ℹ️ Arduino is already disconnected")

    def update_parameters(self, data):
        decoded_data = data.decode()
        self.decoded_symbol = decoded_data
        self.decoded_message += self.decoded_symbol.strip()
        self.text1.setText(self.decoded_message)
        print(decoded_data)

    def closeEvent(self, event):
        Decoder_API.close()
        event.accept()

def apply_dark_theme(app):
    palette = QPalette()
    palette.setColor(QPalette.Window, QColor(20, 20, 20))               # Almost black
    palette.setColor(QPalette.WindowText, QColor(255, 255, 255))        # White
    palette.setColor(QPalette.Base, QColor(30, 30, 30))                 # Dark gray
    palette.setColor(QPalette.AlternateBase, QColor(40, 40, 40))        # Medium gray
    palette.setColor(QPalette.ToolTipBase, QColor(255, 0, 0))           # Red
    palette.setColor(QPalette.ToolTipText, QColor(255, 255, 255))       # White
    palette.setColor(QPalette.Text, QColor(255, 255, 255))              # White
    palette.setColor(QPalette.Button, QColor(50, 50, 50))               # Gray
    palette.setColor(QPalette.ButtonText, QColor(255, 255, 255))        # White
    palette.setColor(QPalette.BrightText, QColor(255, 0, 0))            # Red
    palette.setColor(QPalette.Highlight, QColor(200, 0, 0))             # Dark red
    palette.setColor(QPalette.HighlightedText, QColor(255, 255, 255))   # White
    app.setPalette(palette)

    app.setStyleSheet("""
        QMainWindow {
            background-color: #1a1a1a;
            border: 3px solid #ff0000;
        }
        
        QWidget {
            font-size: 24px;
            color: #ffffff;
            background-color: #1a1a1a;
        }
        
        QPushButton {
            background-color: #333333;
            color: #ffffff;
            border: 3px solid #ff0000;
            border-radius: 10px;
            padding: 12px;
            font-weight: bold;
            font-size: 24px;
            outline: 2px solid #cc0000;
        }
        
        QPushButton:hover {
            background-color: #444444;
            border: 3px solid #ff3333;
            outline: 2px solid #ff0000;
        }
        
        QPushButton:checked {
            background-color: #cc0000;
            color: #ffffff;
            border: 3px solid #ff0000;
            outline: 2px solid #ffffff;
        }
        
        QPushButton:checked:hover {
            background-color: #ff0000;
            border: 3px solid #ff3333;
            outline: 2px solid #ffffff;
        }
        
        QLineEdit {
            background-color: #2d2d2d;
            color: #ffffff;
            border: 3px solid #ff0000;
            border-radius: 8px;
            padding: 8px;
            font-size: 34px;
            selection-background-color: #cc0000;
            outline: 1px solid #cc0000;
        }
        
        QLineEdit:focus {
            border: 3px solid #ff3333;
            outline: 2px solid #ffffff;
        }
        
        QLineEdit:placeholder {
            color: #888888;
            font-style: italic;
        }
        
        QLabel {
            color: #ffffff;
            font-size: 24px;
            padding: 5px;
            border: 1px solid #ff0000;
            border-radius: 5px;
            background-color: #2a2a2a;
        }
        
        QLabel[objectName="logo_label"] {
            border: 3px solid #ff0000;
            border-radius: 10px;
            background-color: #2a2a2a;
            padding: 10px;
        }
        
        QLineEdit[readOnly="true"] {
            background-color: #2a2a2a;
            border: 3px solid #ff0000;
            color: #ffffff;
            font-weight: bold;
            outline: 1px solid #cc0000;
        }
        
        #value_red {
            border: 3px solid #ff0000;
            background-color: #332222;
            outline: 2px solid #cc0000;
        }
        
        #value_green {
            border: 3px solid #ff0000;
            background-color: #223322;
            outline: 2px solid #cc0000;
        }
        
        #value_blue {
            border: 3px solid #ff0000;
            background-color: #222233;
            outline: 2px solid #cc0000;
        }
        
        #value_clear {
            border: 3px solid #ff0000;
            background-color: #333333;
            outline: 2px solid #cc0000;
        }
        
        QGroupBox {
            color: #ff0000;
            font-weight: bold;
            border: 2px solid #ff0000;
            border-radius: 8px;
            margin-top: 10px;
            padding-top: 10px;
            background-color: #2a2a2a;
        }
        
        QGroupBox::title {
            color: #ff0000;
            subcontrol-origin: margin;
            left: 10px;
            padding: 0 5px 0 5px;
        }
    """)
    
    app.setStyleSheet(app.styleSheet() + """
        SimpleWindow {
            background: qlineargradient(x1: 0, y1: 0, x2: 0, y2: 1,
                stop: 0 #1a1a1a, stop: 1 #2a2a2a);
            border: 4px solid #ff0000;
            border-radius: 12px;
        }
        
        QGridLayout {
            border: 2px solid #ff0000;
            border-radius: 8px;
            padding: 10px;
            background-color: #252525;
        }
    """)

if __name__ == '__main__':
    app = QApplication(sys.argv)

    window = SimpleWindow()
    
    apply_dark_theme(app)
    window.show()
    sys.exit(app.exec_())
