
#ifndef TRACH_GUIUPDATER_H
#define TRACH_GUIUPDATER_H


#include <QApplication>
#include "Src/Core/GameState.h"

class GUIUpdater : public QObject {
    Q_OBJECT

public:
    GUIUpdater(QObject *parent = nullptr) : QObject(parent) {}
    void newLabel(GameState* state, int playerId)
    {
        emit sendUpdate(state);
    }

    signals:
            void sendUpdate(GameState* state);
};



#endif //TRACH_GUIUPDATER_H
