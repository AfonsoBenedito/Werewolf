

interface RoleInfoProps {
    role: string;
    isAlive: boolean;
}

export function RoleInfo({ role, isAlive }: RoleInfoProps) {
    return (
        <div className="my-role-card">
            <h3>You are: <span className="role-reveal">{role}</span></h3>
            {!isAlive && <div className="dead-tag">YOU ARE DEAD</div>}
        </div>
    );
}
