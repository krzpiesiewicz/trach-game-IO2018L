#include <QtWidgets>
#include <UI/PlayerInfoUI.h>

int main(int argc, char *argv[])
{
    QApplication app(argc, argv);
    QWidget window;
    window.resize(1280, 768);
    window.setWindowTitle("Trach v0.1");

    auto playerInfo = new PlayerInfoUI(&window, "Lemures64", 5);

    window.show();
    return app.exec();
}