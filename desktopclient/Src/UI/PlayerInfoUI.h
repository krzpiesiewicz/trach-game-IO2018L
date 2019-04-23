
#ifndef TRACH_PLAYERINFOUI_H
#define TRACH_PLAYERINFOUI_H


#include <QtWidgets/QWidget>
#include <QtWidgets/QLabel>
#include "HealthBarUI.h"

class PlayerInfoUI : public QWidget
{
private:

    QLabel* nickLabel;
    HealthBarUI* healthBarUI;

public:
    PlayerInfoUI(QWidget* parent, const std::string nick, int healthValue)
            : QWidget(parent)
    {
        nickLabel = new QLabel(this);
        nickLabel->setText(QString::fromStdString(nick));

        healthBarUI = new HealthBarUI(this, 5);
        healthBarUI->move(0, 50);
    }

    void updateHealthLevel(int newValue)
    {
        healthBarUI->setHealthValue(newValue);
    }
};


#endif //TRACH_PLAYERINFOUI_H
